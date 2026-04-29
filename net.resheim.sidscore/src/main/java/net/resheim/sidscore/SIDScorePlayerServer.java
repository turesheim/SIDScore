/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.LineUnavailableException;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.resheim.sidscore.ir.RealtimeAudioPlayer;
import net.resheim.sidscore.ir.SIDScoreIR;
import net.resheim.sidscore.ir.ScoreBuildingListener;
import net.resheim.sidscore.midi.MidiInputRouter;
import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;
import net.resheim.sidscore.server.ScoreMapExporter;
import net.resheim.sidscore.server.SrapProtocol;
import net.resheim.sidscore.sid.SidModel;

public final class SIDScorePlayerServer {
	private static final String MIDI_AWT_EVENT_PUMP_PROPERTY = "sidscore.midi.awtEventPump";
	private static final int DEFAULT_SCOPE_BUCKETS = 64;
	private static final int OUTBOUND_QUEUE_SIZE = 512;
	private static final int STDOUT_LOG_QUEUE_SIZE = 1024;
	private static final int INSTRUMENT_SOURCE_DEFAULT = 0;
	private static final int INSTRUMENT_SOURCE_SCORE = 1;
	private static final int INSTRUMENT_SOURCE_OVERRIDE = 2;
	private static final String SERVER_USAGE = "Usage: java SIDScoreCLI --player-server [--port <port>] "
			+ "[--midi] [--midi-device <index|name>] [--midi-map <voice:channel,...>]";
	private static final SIDScoreIR.InstrumentIR DEFAULT_SERVER_INSTRUMENT =
			new SIDScoreIR.InstrumentIR("server_default", SIDScoreIR.Wave.PULSE.mask,
					new SIDScoreIR.AdsrIR(0, 4, 10, 4), OptionalInt.of(0x0800),
					OptionalInt.of(0x0000), OptionalInt.of(0x0FFF), 0,
					Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
					0, OptionalInt.empty(), OptionalInt.empty(), Optional.empty(),
					SIDScoreIR.InstrumentGateMode.RETRIGGER, 0, false, false);
	private static final BlockingQueue<String> STDOUT_LOGS = new ArrayBlockingQueue<>(STDOUT_LOG_QUEUE_SIZE);
	private static final AtomicBoolean STDOUT_LOGGER_STARTED = new AtomicBoolean(false);

	private final int requestedPort;
	private final BlockingQueue<OutboundFrame> outbound = new ArrayBlockingQueue<>(OUTBOUND_QUEUE_SIZE);
	private final AtomicLong outboundSequence = new AtomicLong(1);
	private final AtomicLong scoreIds = new AtomicLong(1);
	private final AtomicReferenceArray<SIDScoreIR.InstrumentIR> instrumentOverrides = new AtomicReferenceArray<>(3);
	private final Object midiMonitorRestartLock = new Object();
	private final Object sharedMidiSourceLock = new Object();
	private final MidiVoiceAssignment[] midiVoiceAssignments = new MidiVoiceAssignment[] {
			MidiVoiceAssignment.disabled(1),
			MidiVoiceAssignment.disabled(2),
			MidiVoiceAssignment.disabled(3)
	};

	private volatile boolean running = true;
	private volatile boolean protocolReady = false;
	private volatile int clientCapabilities = SrapProtocol.CAP_ALL;
	private volatile int playbackState = SrapProtocol.STATE_IDLE;
	private volatile boolean midiEnabled = false;
	private volatile boolean pendingMidiMonitorRestart = false;
	private volatile long pendingMidiMonitorRestartRequestId = 0;
	private volatile long currentScoreId = 0;
	private volatile ScoreMapExporter.ScoreMap currentScoreMap = null;
	private volatile LoadedScore currentLoadedScore = null;
	private volatile ServerMidiSource sharedMidiSource = null;
	private volatile List<MidiVoiceAssignment> sharedMidiSourceAssignments = List.of();
	private volatile RealtimeAudioPlayer currentPlayer = null;
	private volatile Thread currentPlayerThread = null;
	private volatile boolean currentMidiMonitor = false;
	private volatile boolean stopRequestedByClient = false;
	private volatile long lastVoiceBlockIndex = -1;
	private volatile long lastVoiceFrameIndex = 0;
	private volatile float lastVoiceSampleRate = 44100.0f;
	private final int[] lastHighlightIds = { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };

	public SIDScorePlayerServer(int requestedPort) {
		this.requestedPort = requestedPort;
	}

	private SIDScorePlayerServer(int requestedPort, boolean midiEnabled, String midiDeviceSelector,
			Map<Integer, Integer> midiVoiceMap) {
		this.requestedPort = requestedPort;
		configureInitialMidiSettings(midiEnabled, midiDeviceSelector, midiVoiceMap);
	}

	public static void main(String[] args) throws Exception {
		enableProtocolMidiEventPump();
		int port = 0;
		boolean midiEnabled = false;
		String midiDeviceSelector = "";
		Map<Integer, Integer> midiVoiceMap = MidiInputRouter.defaultVoiceChannelMap();
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "--port" -> {
				if (i + 1 >= args.length) {
					throw new IllegalArgumentException("--port requires a value");
				}
				port = Integer.parseInt(args[++i]);
			}
			case "--midi" -> midiEnabled = true;
			case "--midi-device" -> {
				if (i + 1 >= args.length) {
					throw new IllegalArgumentException("--midi-device requires a value");
				}
				midiDeviceSelector = args[++i];
				midiEnabled = true;
			}
			case "--midi-map" -> {
				if (i + 1 >= args.length) {
					throw new IllegalArgumentException("--midi-map requires a value");
				}
				midiVoiceMap = MidiInputRouter.parseVoiceChannelMap(args[++i]);
				midiEnabled = true;
			}
			case "--list-midi-devices" -> {
				printMidiDevices();
				return;
			}
			default -> throw new IllegalArgumentException("Unknown player server option: " + args[i]
					+ System.lineSeparator() + SERVER_USAGE);
			}
		}
		new SIDScorePlayerServer(port, midiEnabled, midiDeviceSelector, midiVoiceMap).run();
	}

	private static void enableProtocolMidiEventPump() {
		if (System.getProperty(MIDI_AWT_EVENT_PUMP_PROPERTY) == null) {
			System.setProperty(MIDI_AWT_EVENT_PUMP_PROPERTY, "true");
		}
	}

	private void configureInitialMidiSettings(boolean enabled, String deviceSelector,
			Map<Integer, Integer> voiceChannelMap) {
		MidiVoiceAssignment[] updated = new MidiVoiceAssignment[] {
				MidiVoiceAssignment.disabled(1),
				MidiVoiceAssignment.disabled(2),
				MidiVoiceAssignment.disabled(3)
		};
		Map<Integer, Integer> source = voiceChannelMap == null || voiceChannelMap.isEmpty()
				? MidiInputRouter.defaultVoiceChannelMap()
				: voiceChannelMap;
		for (var entry : source.entrySet()) {
			int voiceIndex = entry.getKey();
			int channel = entry.getValue();
			if (!isValidVoiceIndex(voiceIndex)) {
				throw new IllegalArgumentException("MIDI voice index must be 1..3, got " + voiceIndex);
			}
			if (channel < 1 || channel > 16) {
				throw new IllegalArgumentException("MIDI channel must be 1..16, got " + channel);
			}
			updated[voiceIndex - 1] = new MidiVoiceAssignment(voiceIndex, true,
					deviceSelector != null ? deviceSelector.trim() : "", channel);
		}
		synchronized (midiVoiceAssignments) {
			System.arraycopy(updated, 0, midiVoiceAssignments, 0, midiVoiceAssignments.length);
			this.midiEnabled = enabled;
		}
	}

	private static void printMidiDevices() {
		List<MidiInputRouter.InputDevice> devices = MidiInputRouter.listInputDevices();
		if (devices.isEmpty()) {
			System.out.println("No MIDI input devices found.");
			return;
		}
		System.out.println("Available MIDI input devices:");
		for (MidiInputRouter.InputDevice device : devices) {
			System.out.println("  [" + device.index() + "] " + device.displayName()
					+ " (" + device.vendor() + " / " + device.description() + ")");
		}
	}

	public void run() throws Exception {
		InetAddress loopback = InetAddress.getByName("127.0.0.1");
		try (ServerSocket server = new ServerSocket(requestedPort, 1, loopback)) {
			int port = server.getLocalPort();
			System.out.println("{\"event\":\"ready\",\"protocol\":\"srap-server\",\"version\":1,\"port\":" + port
					+ "}");
			System.out.flush();
			logMidi("server ready; " + midiStateDescription());
			startMidiMonitorIfNeeded(0);
			try (Socket socket = server.accept()) {
				socket.setTcpNoDelay(true);
				Thread writer = new Thread(() -> writerLoop(socket), "sidscore-srap-writer");
				writer.setDaemon(true);
				writer.start();
				try {
						readerLoop(socket);
					} finally {
						running = false;
						stopCurrent(0, false);
						closeSharedMidiSource();
						outbound.clear();
					outbound.offer(OutboundFrame.poisonFrame());
					writer.join(1000);
				}
			}
		}
	}

	private void readerLoop(Socket socket) throws IOException {
		boolean handshaken = false;
		while (running) {
			SrapProtocol.Frame frame;
				try {
					frame = SrapProtocol.readFrame(socket.getInputStream());
				} catch (EOFException eof) {
					break;
				}
				logIncomingSignal(frame);
				try {
					if (!handshaken) {
						if (frame.type() != SrapProtocol.HELLO) {
						enqueueError(0, SrapProtocol.ERR_INVALID_STATE, "HELLO must be the first frame", true);
						break;
					}
					handleHello(frame);
					handshaken = true;
					continue;
				}
				handleCommand(frame);
			} catch (RuntimeException e) {
				enqueueError(0, SrapProtocol.ERR_INVALID_FRAME, e.getMessage(), true);
			}
		}
	}

	private void handleHello(SrapProtocol.Frame frame) {
		SrapProtocol.PayloadReader in = SrapProtocol.reader(frame.payload());
		String clientName = in.str();
		int minVersion = in.u16();
		int maxVersion = in.u16();
		int caps = (int) in.u32();
		if (minVersion > SrapProtocol.VERSION || maxVersion < SrapProtocol.VERSION) {
			enqueueError(0, SrapProtocol.ERR_UNSUPPORTED_VERSION, "Unsupported client protocol range from " + clientName,
					true);
			running = false;
			return;
		}
		protocolReady = true;
		clientCapabilities = caps & SrapProtocol.CAP_ALL;
		byte[] payload = SrapProtocol.payload()
				.u16(SrapProtocol.VERSION)
				.u32(SrapProtocol.CAP_ALL)
				.str("SIDScore Player Server")
				.toByteArray();
		enqueue(SrapProtocol.HELLO_ACK, payload, true);
		logMidi("client connected: " + clientName + "; capabilities=0x" + Integer.toHexString(clientCapabilities));
		sendAllInstrumentStates(0, true);
		sendMidiState(0, true);
		startMidiMonitorIfNeeded(0);
	}

	private void handleCommand(SrapProtocol.Frame frame) {
		switch (frame.type()) {
		case SrapProtocol.PLAY -> handlePlay(frame.payload());
		case SrapProtocol.PLAY_SOURCE -> handlePlaySource(frame.payload());
		case SrapProtocol.PAUSE -> handlePause(frame.payload());
		case SrapProtocol.CONTINUE -> handleContinue(frame.payload());
		case SrapProtocol.STOP -> handleStop(frame.payload());
		case SrapProtocol.SET_INSTRUMENT -> handleSetInstrument(frame.payload());
		case SrapProtocol.RESET_INSTRUMENT -> handleResetInstrument(frame.payload());
		case SrapProtocol.SCAN_MIDI_DEVICES -> handleScanMidiDevices(frame.payload());
		case SrapProtocol.SET_MIDI_SETTINGS -> handleSetMidiSettings(frame.payload());
		default -> enqueueError(0, SrapProtocol.ERR_INVALID_FRAME, "Unsupported frame type: " + frame.type(), true);
		}
	}

	private void handlePlay(byte[] payload) {
		SrapProtocol.PayloadReader in = SrapProtocol.reader(payload);
		long requestId = in.u32();
		String sourceUri = in.str();
		String sourcePathRaw = in.str();
		int sidModelRaw = in.u8();
		in.u8();
		in.u8();
		in.u8();

		Path sourcePath = Path.of(sourcePathRaw).toAbsolutePath().normalize();
		if (!Files.isRegularFile(sourcePath)) {
			enqueueError(requestId, SrapProtocol.ERR_FILE_NOT_FOUND, "File not found: " + sourcePath, true);
			return;
		}
		if (sourceUri == null || sourceUri.isBlank()) {
			sourceUri = sourcePath.toUri().toString();
		}

		String sourceText;
		try {
			sourceText = Files.readString(sourcePath);
		} catch (IOException e) {
			enqueueError(requestId, SrapProtocol.ERR_FILE_NOT_FOUND, "Failed to read file: " + sourcePath, true);
			return;
		}
		startPlayback(requestId, sourceUri, sourcePath, sourceText, sidModelRaw);
	}

	private void handlePlaySource(byte[] payload) {
		SrapProtocol.PayloadReader in = SrapProtocol.reader(payload);
		long requestId = in.u32();
		String sourceUri = in.str();
		String sourcePathRaw = in.str();
		int sidModelRaw = in.u8();
		in.u8();
		in.u8();
		in.u8();
		int sourceLength = (int) in.u32();
		String sourceText = new String(in.bytes(sourceLength), StandardCharsets.UTF_8);
		Path sourcePath = sourcePathFromHint(sourcePathRaw);
		if (sourceUri == null || sourceUri.isBlank()) {
			sourceUri = sourcePathRaw == null || sourcePathRaw.isBlank()
					? "memory://sidscore/current.sidscore"
					: sourcePath.toUri().toString();
		}
		startPlayback(requestId, sourceUri, sourcePath, sourceText, sidModelRaw);
	}

	private void startPlayback(long requestId, String sourceUri, Path sourcePath, String sourceText, int sidModelRaw) {
		stopCurrent(0, false);
		long scoreId = scoreIds.getAndIncrement();
		currentScoreId = scoreId;
		currentLoadedScore = null;
		currentScoreMap = null;
		stopRequestedByClient = false;
		lastVoiceBlockIndex = -1;
		lastVoiceFrameIndex = 0;
		lastVoiceSampleRate = 44100.0f;
		resetHighlightIds();
		sendPlaybackState(requestId, SrapProtocol.STATE_LOADING, SrapProtocol.REASON_CLIENT_REQUEST, scoreId, 0, 0,
				true);
		sendSilentVoiceState(scoreId, true);

		ParsedScore parsed;
		try {
			parsed = parse(sourcePath, sourceText);
		} catch (ScoreBuildingListener.ValidationException e) {
			sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PARSE_ERROR, scoreId, 0, 0, true);
			enqueueError(requestId, SrapProtocol.ERR_PARSE_ERROR, e.getMessage(), true);
			return;
		} catch (IllegalStateException e) {
			sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PARSE_ERROR, scoreId, 0, 0, true);
			enqueueError(requestId, SrapProtocol.ERR_RESOLVE_ERROR, e.getMessage(), true);
			return;
		} catch (Exception e) {
			sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PARSE_ERROR, scoreId, 0, 0, true);
			enqueueError(requestId, SrapProtocol.ERR_PARSE_ERROR, e.getMessage(), true);
			return;
		}

		SidModel sidModel = switch (sidModelRaw) {
		case 2 -> SidModel.MOS8580;
		default -> SidModel.MOS6581;
		};
		LoadedScore loaded = new LoadedScore(sourceUri, sourcePath, parsed.tree(), parsed.timedScore(), sidModel);
		currentLoadedScore = loaded;
		startResolvedPlayback(requestId, scoreId, loaded);
	}

	private void startResolvedPlayback(long requestId, long scoreId, LoadedScore loaded) {
		SIDScoreIR.TimedScore timed = applyInstrumentOverrides(loaded.timedScore());
		ScoreMapExporter.ScoreMap scoreMap = ScoreMapExporter.build(scoreId, loaded.tree(), timed,
				loaded.sourceUri(), loaded.sourcePath());
		currentScoreMap = scoreMap;
		if ((clientCapabilities & SrapProtocol.CAP_SCORE_MAP) != 0) {
			enqueue(SrapProtocol.SCORE_MAP, encodeScoreMap(scoreMap), true);
		}
		sendAllInstrumentStates(requestId, true);
		sendSilentVoiceState(scoreId, true);

		RealtimeAudioPlayer player = new RealtimeAudioPlayer(loaded.sidModel());
		player.setInstrumentProvider(this::effectivePlaybackInstrument);
		currentPlayer = player;
		Thread thread = new Thread(() -> runPlayer(requestId, scoreId, player, timed),
				"sidscore-srap-player");
		currentPlayerThread = thread;
		thread.start();
	}

	private static Path sourcePathFromHint(String sourcePathRaw) {
		if (sourcePathRaw == null || sourcePathRaw.isBlank()) {
			return Path.of("").toAbsolutePath().normalize();
		}
		return Path.of(sourcePathRaw).toAbsolutePath().normalize();
	}

	private void handlePause(byte[] payload) {
		long requestId = SrapProtocol.reader(payload).u32();
		RealtimeAudioPlayer player = currentPlayer;
		if (player == null || playbackState != SrapProtocol.STATE_PLAYING) {
			enqueueError(requestId, SrapProtocol.ERR_INVALID_STATE, "No playing score to pause", true);
			return;
		}
		player.pause();
		sendPlaybackState(requestId, SrapProtocol.STATE_PAUSED, SrapProtocol.REASON_CLIENT_REQUEST, currentScoreId, 0, 0,
				true);
	}

	private void handleContinue(byte[] payload) {
		long requestId = SrapProtocol.reader(payload).u32();
		RealtimeAudioPlayer player = currentPlayer;
		if (player == null || playbackState != SrapProtocol.STATE_PAUSED) {
			enqueueError(requestId, SrapProtocol.ERR_INVALID_STATE, "No paused score to continue", true);
			return;
		}
		player.resume();
		sendPlaybackState(requestId, SrapProtocol.STATE_PLAYING, SrapProtocol.REASON_CLIENT_REQUEST, currentScoreId, 0,
				0, true);
	}

	private void handleStop(byte[] payload) {
		long requestId = SrapProtocol.reader(payload).u32();
		if (currentMidiMonitor) {
			logMidi("STOP received while MIDI monitor is active; leaving MIDI monitor running");
			startMidiMonitorIfNeeded(requestId);
			return;
		}
		logMidi("STOP received; stopping score playback");
		stopRequestedByClient = true;
		stopCurrent(requestId, true);
		stopRequestedByClient = false;
		startMidiMonitorIfNeeded(requestId);
	}

	private void handleSetInstrument(byte[] payload) {
		SrapProtocol.PayloadReader in = SrapProtocol.reader(payload);
		long requestId = in.u32();
		int voiceIndex = in.u8();
		if (!isValidVoiceIndex(voiceIndex)) {
			enqueueError(requestId, SrapProtocol.ERR_INVALID_FRAME, "Instrument voice index must be 1..3", true);
			return;
		}
		SIDScoreIR.InstrumentIR instrument = decodeInstrument(voiceIndex, in);
		instrumentOverrides.set(voiceIndex - 1, instrument);
		sendInstrumentState(requestId, voiceIndex, true);
		applyRealtimeInstrumentChange(requestId, voiceIndex);
	}

	private void handleResetInstrument(byte[] payload) {
		SrapProtocol.PayloadReader in = SrapProtocol.reader(payload);
		long requestId = in.u32();
		int voiceIndex = in.u8();
		if (!isValidVoiceIndex(voiceIndex)) {
			enqueueError(requestId, SrapProtocol.ERR_INVALID_FRAME, "Instrument voice index must be 1..3", true);
			return;
		}
		instrumentOverrides.set(voiceIndex - 1, null);
		sendInstrumentState(requestId, voiceIndex, true);
		applyRealtimeInstrumentChange(requestId, voiceIndex);
	}

	private void handleScanMidiDevices(byte[] payload) {
		long requestId = SrapProtocol.reader(payload).u32();
		List<MidiInputRouter.InputDevice> devices = MidiInputRouter.listInputDevices();
		logMidi("scan found " + devices.size() + " input device(s): " + describeMidiDevices(devices));
		boolean autoConnected = autoConnectSingleMidiDevice(devices);
		enqueue(SrapProtocol.MIDI_DEVICE_LIST, encodeMidiDeviceList(requestId, devices), true);
		if (devices.size() == 1) {
			sendMidiState(requestId, true);
			if (autoConnected) {
				restartRealtimeOutputAfterSettingsChange(requestId);
			} else {
				startMidiMonitorIfNeeded(requestId);
			}
		} else if (autoConnected) {
			sendMidiState(requestId, true);
			restartRealtimeOutputAfterSettingsChange(requestId);
		}
	}

	private void handleSetMidiSettings(byte[] payload) {
		SrapProtocol.PayloadReader in = SrapProtocol.reader(payload);
		long requestId = in.u32();
		boolean enabled = in.u8() != 0;
		int assignmentCount = in.u8();
		in.u16();

		MidiVoiceAssignment[] updated = midiAssignmentsSnapshot();
		boolean[] seen = new boolean[4];
		for (int i = 0; i < assignmentCount; i++) {
			int voiceIndex = in.u8();
			boolean voiceEnabled = in.u8() != 0;
			int channel = in.u8();
			in.u8();
			String deviceSelector = in.str();
			if (!isValidVoiceIndex(voiceIndex)) {
				enqueueError(requestId, SrapProtocol.ERR_INVALID_FRAME, "MIDI voice index must be 1..3", true);
				return;
			}
			if (seen[voiceIndex]) {
				enqueueError(requestId, SrapProtocol.ERR_INVALID_FRAME,
						"MIDI voice appears more than once: " + voiceIndex, true);
				return;
			}
			seen[voiceIndex] = true;
			if (voiceEnabled) {
				if (channel < 1 || channel > 16) {
					enqueueError(requestId, SrapProtocol.ERR_INVALID_FRAME,
							"MIDI channel must be 1..16, got " + channel, true);
					return;
				}
				updated[voiceIndex - 1] = new MidiVoiceAssignment(voiceIndex, true,
						deviceSelector != null ? deviceSelector.trim() : "", channel);
			} else {
				updated[voiceIndex - 1] = MidiVoiceAssignment.disabled(voiceIndex);
			}
		}
		if (enabled && !hasEnabledMidiAssignment(updated)) {
			enqueueError(requestId, SrapProtocol.ERR_INVALID_FRAME,
					"MIDI enabled requires at least one assigned SID voice", true);
			return;
		}

		boolean changed;
		synchronized (midiVoiceAssignments) {
			changed = midiSettingsChangedLocked(enabled, updated);
			if (changed) {
				System.arraycopy(updated, 0, midiVoiceAssignments, 0, midiVoiceAssignments.length);
				midiEnabled = enabled;
			}
		}
		logMidi((changed ? "settings updated" : "settings unchanged") + " by protocol; "
				+ midiStateDescription());
		sendMidiState(requestId, true);
		if (changed) {
			restartRealtimeOutputAfterSettingsChange(requestId);
		} else {
			startMidiMonitorIfNeeded(requestId);
		}
	}

	private void restartRealtimeOutputAfterSettingsChange(long requestId) {
		if (restartLoadedScoreIfActive(requestId)) {
			return;
		}
		if (currentMidiMonitor) {
			boolean shouldRestart = !enabledMidiAssignments().isEmpty();
			logMidi("restarting MIDI monitor after settings change; shouldRestart=" + shouldRestart);
			if (shouldRestart) {
				queueMidiMonitorRestart(requestId);
			}
			stopRequestedByClient = true;
			boolean stopped = stopCurrent(shouldRestart ? 0 : requestId, !shouldRestart);
			stopRequestedByClient = false;
			if (shouldRestart && stopped) {
				startPendingMidiMonitorRestart();
			}
			return;
		}
		startMidiMonitorIfNeeded(requestId);
	}

	private void applyRealtimeInstrumentChange(long requestId, int voiceIndex) {
		Thread thread = currentPlayerThread;
		RealtimeAudioPlayer player = currentPlayer;
		int state = playbackState;
		if (player != null && thread != null
				&& (state == SrapProtocol.STATE_PLAYING || state == SrapProtocol.STATE_PAUSED)) {
			logMidi("applied live instrument change for voice " + voiceIndex + " without restarting realtime output");
			if (currentMidiMonitor) {
				sendCurrentMidiMonitorState(requestId);
			}
			return;
		}
		startMidiMonitorIfNeeded(requestId);
	}

	private boolean restartLoadedScoreIfActive(long requestId) {
		if (currentMidiMonitor) {
			return false;
		}
		int state = playbackState;
		if (state != SrapProtocol.STATE_PLAYING && state != SrapProtocol.STATE_PAUSED) {
			return false;
		}
		LoadedScore loaded = currentLoadedScore;
		if (loaded == null) {
			return false;
		}
		logMidi("restarting loaded score after realtime MIDI/instrument change");
		stopRequestedByClient = true;
		stopCurrent(0, false);

		long scoreId = scoreIds.getAndIncrement();
		currentScoreId = scoreId;
		stopRequestedByClient = false;
		lastVoiceBlockIndex = -1;
		lastVoiceFrameIndex = 0;
		lastVoiceSampleRate = 44100.0f;
		resetHighlightIds();
		sendPlaybackState(requestId, SrapProtocol.STATE_LOADING, SrapProtocol.REASON_CLIENT_REQUEST, scoreId, 0, 0,
				true);
		sendSilentVoiceState(scoreId, true);
		startResolvedPlayback(requestId, scoreId, loaded);
		return true;
	}

	private void runPlayer(long requestId, long scoreId, RealtimeAudioPlayer player, SIDScoreIR.TimedScore timed) {
		ServerMidiSource midiSource = null;
		boolean endedNormally = false;
		try {
				midiSource = openSharedMidiSourceIfEnabled();
			if (midiSource != null) {
				logMidi("score playback using MIDI source: " + midiSource.description());
				sendMidiState(requestId, true);
			}
			sendPlaybackState(requestId, SrapProtocol.STATE_PLAYING, SrapProtocol.REASON_CLIENT_REQUEST, scoreId, 0, 0,
					true);
			player.playWithTelemetry(timed, block -> handlePlaybackBlock(scoreId, block), midiSource);
			if (!stopRequestedByClient && running && currentScoreId == scoreId) {
				sendSilentVoiceState(scoreId, true);
				sendHighlightState(scoreId, 0, -1, -1, -1, true);
				sendPlaybackState(0, SrapProtocol.STATE_ENDED, SrapProtocol.REASON_END_OF_SCORE, scoreId, 0, 0, true);
				endedNormally = true;
			}
		} catch (MidiUnavailableException e) {
			logMidi("MIDI input failed during score playback: " + e.getMessage());
			sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PLAYBACK_ERROR, scoreId, 0, 0,
					true);
			enqueueError(requestId, SrapProtocol.ERR_PLAYBACK_ERROR, "MIDI input failed: " + e.getMessage(), true);
		} catch (LineUnavailableException e) {
			sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PLAYBACK_ERROR, scoreId, 0, 0,
					true);
			enqueueError(requestId, SrapProtocol.ERR_PLAYBACK_ERROR, e.getMessage(), true);
		} finally {
				closeSharedMidiSourceIfDisabled();
				if (currentPlayer == player) {
				currentPlayer = null;
				currentPlayerThread = null;
				currentMidiMonitor = false;
				if (endedNormally) {
					startMidiMonitorIfNeeded(0);
				}
			}
		}
	}

	private void startMidiMonitorIfNeeded(long requestId) {
		if (currentPlayerThread != null || enabledMidiAssignments().isEmpty()) {
			if (currentPlayerThread != null) {
				logMidi("MIDI monitor not started; current player thread is active");
				if (currentMidiMonitor && protocolReady) {
					sendCurrentMidiMonitorState(requestId);
				}
			} else {
				logMidi("MIDI monitor not started; no enabled MIDI assignments");
			}
			return;
		}
		long scoreId = scoreIds.getAndIncrement();
		currentScoreId = scoreId;
		currentScoreMap = null;
		stopRequestedByClient = false;
		lastVoiceBlockIndex = -1;
		lastVoiceFrameIndex = 0;
		lastVoiceSampleRate = 44100.0f;
		resetHighlightIds();
		if (protocolReady) {
			sendSilentVoiceState(scoreId, true);
		}

		SIDScoreIR.TimedScore timed = buildMidiMonitorScore();
		logMidi("starting MIDI monitor scoreId=" + scoreId + "; " + midiStateDescription()
				+ "; instruments=" + midiInstrumentDescription(timed));
		SidModel sidModel = currentLoadedScore != null ? currentLoadedScore.sidModel() : SidModel.MOS6581;
		RealtimeAudioPlayer player = new RealtimeAudioPlayer(sidModel);
		player.setInstrumentProvider(this::effectivePlaybackInstrument);
		currentPlayer = player;
		currentMidiMonitor = true;
		Thread thread = new Thread(() -> runMidiMonitor(requestId, scoreId, player, timed),
				"sidscore-srap-midi-monitor");
		currentPlayerThread = thread;
		thread.start();
	}

	private void runMidiMonitor(long requestId, long scoreId, RealtimeAudioPlayer player, SIDScoreIR.TimedScore timed) {
		ServerMidiSource midiSource = null;
		try {
				midiSource = openSharedMidiSourceIfEnabled();
			if (midiSource == null) {
				logMidi("MIDI monitor stopped before start; no MIDI source available");
				return;
			}
			logMidi("MIDI monitor using source: " + midiSource.description());
			if (protocolReady) {
				sendMidiState(requestId, true);
				sendPlaybackState(requestId, SrapProtocol.STATE_PLAYING, SrapProtocol.REASON_CLIENT_REQUEST, scoreId, 0, 0,
						true);
			} else {
				playbackState = SrapProtocol.STATE_PLAYING;
			}
			player.playWithTelemetry(timed, block -> handlePlaybackBlock(scoreId, block), midiSource);
		} catch (MidiUnavailableException e) {
			logMidi("MIDI input failed during monitor playback: " + e.getMessage());
			if (protocolReady) {
				sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PLAYBACK_ERROR, scoreId, 0, 0,
						true);
				enqueueError(requestId, SrapProtocol.ERR_PLAYBACK_ERROR, "MIDI input failed: " + e.getMessage(), true);
			} else {
				playbackState = SrapProtocol.STATE_ERROR;
			}
		} catch (LineUnavailableException e) {
			if (protocolReady) {
				sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PLAYBACK_ERROR, scoreId, 0, 0,
						true);
				enqueueError(requestId, SrapProtocol.ERR_PLAYBACK_ERROR, e.getMessage(), true);
			} else {
				playbackState = SrapProtocol.STATE_ERROR;
			}
		} finally {
			closeSharedMidiSourceIfDisabled();
			logMidi("MIDI monitor stopped scoreId=" + scoreId);
			boolean clearedCurrentMonitor = false;
			if (currentPlayer == player) {
				currentPlayer = null;
				currentPlayerThread = null;
				currentMidiMonitor = false;
				clearedCurrentMonitor = true;
			}
			if (clearedCurrentMonitor) {
				startPendingMidiMonitorRestart();
			}
		}
	}

	private void sendCurrentMidiMonitorState(long requestId) {
		sendMidiState(requestId, true);
		sendPlaybackState(requestId, SrapProtocol.STATE_PLAYING, SrapProtocol.REASON_CLIENT_REQUEST, currentScoreId,
				lastVoiceFrameIndex, 0, true);
	}

	private SIDScoreIR.TimedScore buildMidiMonitorScore() {
		LoadedScore loaded = currentLoadedScore;
		SIDScoreIR.TimedScore base = loaded != null ? applyInstrumentOverrides(loaded.timedScore()) : null;
		Map<Integer, SIDScoreIR.TimedVoice> voices = new LinkedHashMap<>();
		for (MidiVoiceAssignment assignment : enabledMidiAssignments()) {
			int voiceIndex = assignment.voiceIndex();
			SIDScoreIR.InstrumentIR instrument;
			if (base != null && base.voices().containsKey(voiceIndex)) {
				instrument = base.voices().get(voiceIndex).instrument();
			} else {
				instrument = effectiveInstrumentState(voiceIndex).instrument();
			}
			voices.put(voiceIndex, new SIDScoreIR.TimedVoice(voiceIndex, instrument, List.of()));
		}
		return new SIDScoreIR.TimedScore(
				base != null ? base.title() : Optional.empty(),
				base != null ? base.author() : Optional.empty(),
				base != null ? base.released() : Optional.empty(),
				base != null ? base.tempoBpm() : 120,
				base != null ? base.ticksPerWhole() : SIDScoreIR.Resolver.DEFAULT_TICKS_PER_WHOLE,
				base != null ? base.defaultSwing() : new SIDScoreIR.SwingOff(),
				base != null ? base.system() : SIDScoreIR.VideoSystem.PAL,
				base != null ? base.tables() : Map.of(),
				Map.of(), voices, Map.of());
	}

	private void handlePlaybackBlock(long scoreId, RealtimeAudioPlayer.PlaybackBlock block) {
		if (!running || !protocolReady || currentScoreId != scoreId || stopRequestedByClient) {
			return;
		}
		lastVoiceBlockIndex = block.blockIndex();
		lastVoiceFrameIndex = block.frameIndex();
		lastVoiceSampleRate = block.sampleRate();
		if ((clientCapabilities & SrapProtocol.CAP_VOICE_STATE) != 0) {
			enqueue(SrapProtocol.VOICE_STATE, encodeVoiceState(scoreId, block), false);
		}
		if ((clientCapabilities & SrapProtocol.CAP_SCOPE_BUCKETS) != 0) {
			enqueue(SrapProtocol.SCOPE_BUCKETS, encodeScopeBuckets(scoreId, block), false);
		}
		if ((clientCapabilities & SrapProtocol.CAP_SCOPE_SAMPLES) != 0) {
			enqueue(SrapProtocol.SCOPE_SAMPLES, encodeScopeSamples(scoreId, block), false);
		}
		if ((clientCapabilities & SrapProtocol.CAP_HIGHLIGHT_STATE) != 0) {
			ScoreMapExporter.ScoreMap map = currentScoreMap;
			if (map != null && map.scoreId() == scoreId) {
				int v1 = map.activeEventId(1, block.frameIndex());
				int v2 = map.activeEventId(2, block.frameIndex());
				int v3 = map.activeEventId(3, block.frameIndex());
				if (highlightChanged(v1, v2, v3)) {
					sendHighlightState(scoreId, block.frameIndex(), v1, v2, v3, false);
				}
			}
		}
	}

	private void queueMidiMonitorRestart(long requestId) {
		synchronized (midiMonitorRestartLock) {
			pendingMidiMonitorRestart = true;
			pendingMidiMonitorRestartRequestId = requestId;
		}
	}

	private void startPendingMidiMonitorRestart() {
		long requestId;
		synchronized (midiMonitorRestartLock) {
			if (!pendingMidiMonitorRestart) {
				return;
			}
			pendingMidiMonitorRestart = false;
			requestId = pendingMidiMonitorRestartRequestId;
			pendingMidiMonitorRestartRequestId = 0;
		}
		if (!running || enabledMidiAssignments().isEmpty()) {
			return;
		}
		startMidiMonitorIfNeeded(requestId);
	}

	private boolean stopCurrent(long requestId, boolean emitState) {
		RealtimeAudioPlayer player = currentPlayer;
		if (player != null) {
			player.stop();
		}
		Thread thread = currentPlayerThread;
		boolean stopped = thread == null || thread == Thread.currentThread();
		if (thread != null && thread != Thread.currentThread()) {
			try {
				thread.join(3000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			stopped = !thread.isAlive();
			if (!stopped) {
				logMidi("current player thread is still stopping; deferred restart will run after close");
			}
		}
		if (stopped) {
			boolean stoppingCurrentPlayer = currentPlayer == player || currentPlayerThread == thread;
			if (currentPlayer == player) {
				currentPlayer = null;
			}
			if (currentPlayerThread == thread) {
				currentPlayerThread = null;
			}
			if (stoppingCurrentPlayer) {
				currentMidiMonitor = false;
			}
		}
		resetHighlightIds();
		if (emitState) {
			sendSilentVoiceState(currentScoreId, true);
			sendSilentScopeState(currentScoreId, true);
			sendHighlightState(currentScoreId, 0, -1, -1, -1, true);
			sendPlaybackState(requestId, SrapProtocol.STATE_STOPPED, SrapProtocol.REASON_CLIENT_REQUEST, currentScoreId,
					0, 0, true);
		}
		return stopped;
	}

	private ParsedScore parse(Path sourcePath, String sourceText) throws Exception {
		SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(sourceText != null ? sourceText : ""));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SIDScoreParser parser = new SIDScoreParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(new ThrowingErrorListener());

		SIDScoreParser.FileContext tree = parser.file();
		ScoreBuildingListener builder = new ScoreBuildingListener(sourcePath);
		ParseTreeWalker.DEFAULT.walk(builder, tree);
		SIDScoreIR.ScoreIR scoreIR = builder.buildScoreIR();
		SIDScoreIR.Resolver.Result resolved = new SIDScoreIR.Resolver().resolve(scoreIR);
		return new ParsedScore(tree, resolved.timedScore());
	}

	private SIDScoreIR.InstrumentIR decodeInstrument(int voiceIndex, SrapProtocol.PayloadReader in) {
		int waveMask = normalizeWaveMask(in.u8());
		int attack = clamp(in.u8(), 0, 15);
		int decay = clamp(in.u8(), 0, 15);
		int sustain = clamp(in.u8(), 0, 15);
		int release = clamp(in.u8(), 0, 15);
		int pulseWidth = clamp(in.u16(), 0, 0x0FFF);
		int pulseSweep = clamp(in.i16(), -128, 128);
		int pulseMin = clamp(in.u16(), 0, 0x0FFF);
		int pulseMax = clamp(in.u16(), 0, 0x0FFF);
		if (pulseMin > pulseMax) {
			int tmp = pulseMin;
			pulseMin = pulseMax;
			pulseMax = tmp;
		}
		int filterModeMask = in.u8() & 0x07;
		int filterCutoff = clamp(in.u16(), 0, 0x07FF);
		int filterResonance = clamp(in.u8(), 0, 15);
		SIDScoreIR.InstrumentGateMode gateMode = in.u8() == 1
				? SIDScoreIR.InstrumentGateMode.LEGATO
				: SIDScoreIR.InstrumentGateMode.RETRIGGER;
		int gateMin = clamp(in.u8(), 0, 16);
		boolean sync = in.u8() != 0;
		boolean ring = in.u8() != 0;
		String name = in.str();

		if ((waveMask & SIDScoreIR.Wave.NOISE.mask) != 0) {
			ring = false;
		} else if (ring && (waveMask & SIDScoreIR.Wave.TRI.mask) == 0) {
			waveMask |= SIDScoreIR.Wave.TRI.mask;
		}
		if (name == null || name.isBlank()) {
			name = "server_voice_" + voiceIndex;
		}

		OptionalInt cutoff = filterModeMask != 0 ? OptionalInt.of(filterCutoff) : OptionalInt.empty();
		OptionalInt resonance = filterModeMask != 0 ? OptionalInt.of(filterResonance) : OptionalInt.empty();
		return new SIDScoreIR.InstrumentIR(name, waveMask,
				new SIDScoreIR.AdsrIR(attack, decay, sustain, release),
				OptionalInt.of(pulseWidth), OptionalInt.of(pulseMin), OptionalInt.of(pulseMax), pulseSweep,
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				filterModeMask, cutoff, resonance, Optional.empty(), gateMode, gateMin, sync, ring);
	}

	private SIDScoreIR.TimedScore applyInstrumentOverrides(SIDScoreIR.TimedScore score) {
		boolean hasOverrides = false;
		for (int i = 0; i < instrumentOverrides.length(); i++) {
			if (instrumentOverrides.get(i) != null) {
				hasOverrides = true;
				break;
			}
		}
		if (!hasOverrides) {
			return score;
		}
		Map<Integer, SIDScoreIR.TimedVoice> voices = new LinkedHashMap<>(score.voices());
		for (int voiceIndex = 1; voiceIndex <= 3; voiceIndex++) {
			SIDScoreIR.InstrumentIR override = instrumentOverrides.get(voiceIndex - 1);
			if (override == null) {
				continue;
			}
			SIDScoreIR.TimedVoice existing = voices.get(voiceIndex);
			List<SIDScoreIR.TimedEvent> events = existing != null ? existing.events() : List.of();
			voices.put(voiceIndex, new SIDScoreIR.TimedVoice(voiceIndex, override, events));
		}
		return new SIDScoreIR.TimedScore(score.title(), score.author(), score.released(),
				score.tempoBpm(), score.ticksPerWhole(), score.defaultSwing(), score.system(),
				score.tables(), score.effects(), voices, score.subtunes());
	}

	private ServerMidiSource openSharedMidiSourceIfEnabled() throws MidiUnavailableException {
		List<MidiVoiceAssignment> assignments = enabledMidiAssignments();
		if (assignments.isEmpty()) {
			return null;
		}
		synchronized (sharedMidiSourceLock) {
			ServerMidiSource existing = sharedMidiSource;
			if (existing != null && existing.isOpen()
					&& sameMidiAssignments(sharedMidiSourceAssignments, assignments)) {
				existing.resumeInput();
				logMidi("reusing MIDI input source: " + existing.description());
				return existing;
			}
			closeSharedMidiSourceLocked();
			ServerMidiSource opened = ServerMidiSource.open(assignments);
			sharedMidiSource = opened;
			sharedMidiSourceAssignments = assignments;
			return opened;
		}
	}

	private void closeSharedMidiSourceIfDisabled() {
		if (!enabledMidiAssignments().isEmpty()) {
			return;
		}
		closeSharedMidiSource();
	}

	private void closeSharedMidiSource() {
		synchronized (sharedMidiSourceLock) {
			closeSharedMidiSourceLocked();
		}
	}

	private void closeSharedMidiSourceLocked() {
		ServerMidiSource existing = sharedMidiSource;
		sharedMidiSource = null;
		sharedMidiSourceAssignments = List.of();
		if (existing != null) {
			existing.close();
		}
	}

	private static boolean sameMidiAssignments(List<MidiVoiceAssignment> left, List<MidiVoiceAssignment> right) {
		if (left.size() != right.size()) {
			return false;
		}
		for (int i = 0; i < left.size(); i++) {
			MidiVoiceAssignment a = left.get(i);
			MidiVoiceAssignment b = right.get(i);
			if (a.voiceIndex() != b.voiceIndex() || a.enabled() != b.enabled()
					|| a.channel() != b.channel()) {
				return false;
			}
			if (!midiSelectorsEquivalent(a.deviceSelector(), b.deviceSelector())) {
				return false;
			}
		}
		return true;
	}

	private List<MidiVoiceAssignment> enabledMidiAssignments() {
		List<MidiVoiceAssignment> assignments = new ArrayList<>();
		synchronized (midiVoiceAssignments) {
			if (!midiEnabled) {
				return List.of();
			}
			for (MidiVoiceAssignment assignment : midiVoiceAssignments) {
				if (assignment.enabled()) {
					assignments.add(assignment);
				}
			}
		}
		return List.copyOf(assignments);
	}

	private List<MidiVoiceAssignment> allMidiAssignments() {
		List<MidiVoiceAssignment> assignments = new ArrayList<>();
		synchronized (midiVoiceAssignments) {
			for (MidiVoiceAssignment assignment : midiVoiceAssignments) {
				assignments.add(assignment);
			}
		}
		return List.copyOf(assignments);
	}

	private MidiVoiceAssignment[] midiAssignmentsSnapshot() {
		synchronized (midiVoiceAssignments) {
			return midiVoiceAssignments.clone();
		}
	}

	private static boolean hasEnabledMidiAssignment(MidiVoiceAssignment[] assignments) {
		for (MidiVoiceAssignment assignment : assignments) {
			if (assignment.enabled()) {
				return true;
			}
		}
		return false;
	}

	private boolean autoConnectSingleMidiDevice(List<MidiInputRouter.InputDevice> devices) {
		if (devices.size() != 1) {
			return false;
		}
		String selector = Integer.toString(devices.get(0).index());
		boolean changed = false;
		synchronized (midiVoiceAssignments) {
			MidiVoiceAssignment[] updated = new MidiVoiceAssignment[midiVoiceAssignments.length];
			boolean hasEnabledAssignment = false;
			for (int i = 0; i < midiVoiceAssignments.length; i++) {
				MidiVoiceAssignment assignment = midiVoiceAssignments[i];
				if (assignment.enabled()) {
					hasEnabledAssignment = true;
					if (!midiSelectorsEquivalent(assignment.deviceSelector(), selector)) {
						assignment = new MidiVoiceAssignment(assignment.voiceIndex(), true, selector,
								assignment.channel());
						changed = true;
					}
				}
				updated[i] = assignment;
			}
			if (!hasEnabledAssignment) {
				for (var entry : MidiInputRouter.defaultVoiceChannelMap().entrySet()) {
					int voiceIndex = entry.getKey();
					updated[voiceIndex - 1] = new MidiVoiceAssignment(voiceIndex, true, selector, entry.getValue());
				}
				changed = true;
			}
			if (!midiEnabled) {
				midiEnabled = true;
				changed = true;
			}
			if (changed) {
				System.arraycopy(updated, 0, midiVoiceAssignments, 0, midiVoiceAssignments.length);
				logMidi("auto-connected single MIDI device: selector=" + selector + " name='"
						+ devices.get(0).displayName() + "'; " + midiStateDescription());
			} else {
				logMidi("single MIDI device already connected: selector=" + selector + " name='"
						+ devices.get(0).displayName() + "'; " + midiStateDescription());
			}
		}
		return changed;
	}

	private boolean midiSettingsChangedLocked(boolean enabled, MidiVoiceAssignment[] updated) {
		if (midiEnabled != enabled) {
			return true;
		}
		for (int i = 0; i < midiVoiceAssignments.length; i++) {
			MidiVoiceAssignment current = midiVoiceAssignments[i];
			MidiVoiceAssignment next = updated[i];
			if (current.voiceIndex() != next.voiceIndex() || current.enabled() != next.enabled()
					|| current.channel() != next.channel()) {
				return true;
			}
			if (current.enabled() && !midiSelectorsEquivalent(current.deviceSelector(), next.deviceSelector())) {
				return true;
			}
		}
		return false;
	}

	private String midiStateDescription() {
		return "enabled=" + midiEnabled + "; assignments=" + describeMidiAssignments(allMidiAssignments());
	}

	private static String describeMidiAssignments(List<MidiVoiceAssignment> assignments) {
		if (assignments.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < assignments.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			MidiVoiceAssignment assignment = assignments.get(i);
			sb.append("voice ").append(assignment.voiceIndex()).append('=');
			if (!assignment.enabled()) {
				sb.append("off");
			} else {
				sb.append("ch ").append(assignment.channel())
						.append(" selector='").append(printableMidiSelector(assignment.deviceSelector())).append('\'');
			}
		}
		sb.append(']');
		return sb.toString();
	}

	private static String describeMidiDevices(List<MidiInputRouter.InputDevice> devices) {
		if (devices.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < devices.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			MidiInputRouter.InputDevice device = devices.get(i);
			sb.append(device.index()).append("='").append(device.displayName()).append('\'');
		}
		sb.append(']');
		return sb.toString();
	}

	private static String midiInstrumentDescription(SIDScoreIR.TimedScore timed) {
		if (timed == null || timed.voices().isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for (var entry : timed.voices().entrySet()) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			SIDScoreIR.InstrumentIR instrument = entry.getValue().instrument();
			sb.append("voice ").append(entry.getKey()).append("='")
					.append(instrument != null ? instrument.name() : "none").append('\'');
		}
		sb.append(']');
		return sb.toString();
	}

	private static String printableMidiSelector(String selector) {
		return selector == null || selector.isBlank() ? "<default>" : selector;
	}

	private static void logMidi(String message) {
		enqueueStdoutLog("[sidscore-midi] " + message);
	}

	// Java Sound calls this path from the MIDI callback; keep it independent of stdout back-pressure.
	private static void logMidiEventAsync(String message) {
		enqueueStdoutLog("[sidscore-midi] event " + message);
	}

	private static void logIncomingSignal(SrapProtocol.Frame frame) {
		enqueueStdoutLog("[sidscore-signal] IN " + signalName(frame.type()) + " seq=" + frame.sequence()
				+ " payloadBytes=" + frame.payload().length + signalSummary(frame));
	}

	private static void enqueueStdoutLog(String message) {
		startStdoutLogger();
		if (!STDOUT_LOGS.offer(message)) {
			STDOUT_LOGS.poll();
			STDOUT_LOGS.offer(message);
		}
	}

	private static void startStdoutLogger() {
		if (!STDOUT_LOGGER_STARTED.compareAndSet(false, true)) {
			return;
		}
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					writeStdoutLog(STDOUT_LOGS.take());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}, "sidscore-stdout-log");
		thread.setDaemon(true);
		thread.start();
	}

	private static void writeStdoutLog(String message) {
		System.out.println(message);
		System.out.flush();
	}

	private static String signalSummary(SrapProtocol.Frame frame) {
		try {
			SrapProtocol.PayloadReader in = SrapProtocol.reader(frame.payload());
			return switch (frame.type()) {
			case SrapProtocol.HELLO -> {
				String clientName = in.str();
				int minVersion = in.u16();
				int maxVersion = in.u16();
				int caps = (int) in.u32();
				yield " client='" + clientName + "' versions=" + minVersion + ".." + maxVersion
						+ " caps=0x" + Integer.toHexString(caps);
			}
			case SrapProtocol.PLAY -> {
				long requestId = in.u32();
				String sourceUri = in.str();
				String sourcePath = in.str();
				int sidModel = in.u8();
				yield " request=" + requestId + " sourcePath='" + sourcePath + "' sourceUri='"
						+ sourceUri + "' sidModel=" + sidModel;
			}
			case SrapProtocol.PLAY_SOURCE -> {
				long requestId = in.u32();
				String sourceUri = in.str();
				String sourcePath = in.str();
				int sidModel = in.u8();
				in.u8();
				in.u8();
				in.u8();
				long sourceLength = in.u32();
				yield " request=" + requestId + " sourcePath='" + sourcePath + "' sourceUri='"
						+ sourceUri + "' sidModel=" + sidModel + " sourceBytes=" + sourceLength;
			}
			case SrapProtocol.PAUSE, SrapProtocol.CONTINUE, SrapProtocol.STOP,
					SrapProtocol.SCAN_MIDI_DEVICES -> " request=" + in.u32();
			case SrapProtocol.SET_INSTRUMENT -> signalInstrumentSummary(in, true);
			case SrapProtocol.RESET_INSTRUMENT -> signalInstrumentSummary(in, false);
			case SrapProtocol.SET_MIDI_SETTINGS -> signalMidiSettingsSummary(in);
			default -> "";
			};
		} catch (RuntimeException e) {
			return " summaryError=" + e.getClass().getSimpleName();
		}
	}

	private static String signalInstrumentSummary(SrapProtocol.PayloadReader in, boolean includesInstrument) {
		long requestId = in.u32();
		int voiceIndex = in.u8();
		if (!includesInstrument) {
			return " request=" + requestId + " voice=" + voiceIndex;
		}
		int waveMask = in.u8();
		int attack = in.u8();
		int decay = in.u8();
		int sustain = in.u8();
		int release = in.u8();
		in.u16();
		in.i16();
		in.u16();
		in.u16();
		in.u8();
		in.u16();
		in.u8();
		in.u8();
		in.u8();
		in.u8();
		in.u8();
		String name = in.str();
		return " request=" + requestId + " voice=" + voiceIndex + " instrument='" + name
				+ "' wave=0x" + Integer.toHexString(waveMask) + " adsr=" + attack + "," + decay
				+ "," + sustain + "," + release;
	}

	private static String signalMidiSettingsSummary(SrapProtocol.PayloadReader in) {
		long requestId = in.u32();
		boolean enabled = in.u8() != 0;
		int assignmentCount = in.u8();
		in.u16();
		StringBuilder sb = new StringBuilder();
		sb.append(" request=").append(requestId)
				.append(" enabled=").append(enabled)
				.append(" assignments=").append(assignmentCount)
				.append(" [");
		for (int i = 0; i < assignmentCount; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			int voiceIndex = in.u8();
			boolean voiceEnabled = in.u8() != 0;
			int channel = in.u8();
			in.u8();
			String selector = in.str();
			sb.append("voice ").append(voiceIndex).append('=');
			if (!voiceEnabled) {
				sb.append("off");
			} else {
				sb.append("ch ").append(channel)
						.append(" selector='").append(printableMidiSelector(selector)).append('\'');
			}
		}
		sb.append(']');
		return sb.toString();
	}

	private static String signalName(int type) {
		return switch (type) {
		case SrapProtocol.HELLO -> "HELLO";
		case SrapProtocol.PLAY -> "PLAY";
		case SrapProtocol.PAUSE -> "PAUSE";
		case SrapProtocol.CONTINUE -> "CONTINUE";
		case SrapProtocol.STOP -> "STOP";
		case SrapProtocol.PLAY_SOURCE -> "PLAY_SOURCE";
		case SrapProtocol.SET_INSTRUMENT -> "SET_INSTRUMENT";
		case SrapProtocol.RESET_INSTRUMENT -> "RESET_INSTRUMENT";
		case SrapProtocol.SCAN_MIDI_DEVICES -> "SCAN_MIDI_DEVICES";
		case SrapProtocol.SET_MIDI_SETTINGS -> "SET_MIDI_SETTINGS";
		default -> "0x" + Integer.toHexString(type);
		};
	}

	private void sendMidiState(long requestId, boolean critical) {
		if (requestId == 0 && (clientCapabilities & SrapProtocol.CAP_MIDI_STATE) == 0) {
			return;
		}
		enqueue(SrapProtocol.MIDI_STATE, encodeMidiState(requestId), critical);
	}

	private byte[] encodeMidiState(long requestId) {
		List<MidiVoiceAssignment> assignments = allMidiAssignments();
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u32(requestId)
				.u8(midiEnabled ? 1 : 0)
				.u8(assignments.size())
				.u16(0);
		for (MidiVoiceAssignment assignment : assignments) {
			out.u8(assignment.voiceIndex())
					.u8(assignment.enabled() ? 1 : 0)
					.u8(assignment.channel())
					.u8(0)
					.str(assignment.deviceSelector())
					.str(assignment.enabled() ? resolveMidiDeviceName(assignment.deviceSelector()) : "");
		}
		return out.toByteArray();
	}

	private byte[] encodeMidiDeviceList(long requestId, List<MidiInputRouter.InputDevice> devices) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u32(requestId)
				.u16(devices.size());
		for (MidiInputRouter.InputDevice device : devices) {
			out.u16(device.index())
					.str(Integer.toString(device.index()))
					.str(device.displayName())
					.str(device.name())
					.str(device.vendor())
					.str(device.description())
					.str(device.version());
		}
		return out.toByteArray();
	}

	private static String resolveMidiDeviceName(String selector) {
		List<MidiInputRouter.InputDevice> devices = MidiInputRouter.listInputDevices();
		if (devices.isEmpty()) {
			return "";
		}
		if (selector == null || selector.isBlank()) {
			return devices.get(0).displayName();
		}
		for (MidiInputRouter.InputDevice device : devices) {
			if (midiDeviceMatches(device, selector)) {
				return device.displayName();
			}
		}
		return "";
	}

	private static boolean midiDeviceMatches(MidiInputRouter.InputDevice device, String selector) {
		String trimmed = selector != null ? selector.trim() : "";
		if (trimmed.isEmpty()) {
			return true;
		}
		try {
			return device.index() == Integer.parseInt(trimmed);
		} catch (NumberFormatException ignored) {
			// Match by textual device metadata below.
		}
		String needle = trimmed.toLowerCase(Locale.ROOT);
		return device.displayName().toLowerCase(Locale.ROOT).contains(needle)
				|| device.name().toLowerCase(Locale.ROOT).contains(needle)
				|| device.vendor().toLowerCase(Locale.ROOT).contains(needle)
				|| device.description().toLowerCase(Locale.ROOT).contains(needle)
				|| device.version().toLowerCase(Locale.ROOT).contains(needle);
	}

	private static boolean midiSelectorsEquivalent(String first, String second) {
		String left = first != null ? first.trim() : "";
		String right = second != null ? second.trim() : "";
		if (left.equals(right)) {
			return true;
		}
		String leftKey = midiSelectorDeviceKey(left);
		String rightKey = midiSelectorDeviceKey(right);
		return leftKey != null && leftKey.equals(rightKey);
	}

	private static String midiSelectorDeviceKey(String selector) {
		List<MidiInputRouter.InputDevice> devices = MidiInputRouter.listInputDevices();
		if (devices.isEmpty()) {
			return null;
		}
		for (MidiInputRouter.InputDevice device : devices) {
			if (midiDeviceMatches(device, selector)) {
				return Integer.toString(device.index());
			}
		}
		return null;
	}

	private void sendAllInstrumentStates(long requestId, boolean critical) {
		for (int voiceIndex = 1; voiceIndex <= 3; voiceIndex++) {
			sendInstrumentState(requestId, voiceIndex, critical);
		}
	}

	private void sendInstrumentState(long requestId, int voiceIndex, boolean critical) {
		if ((clientCapabilities & SrapProtocol.CAP_INSTRUMENT_STATE) == 0) {
			return;
		}
		InstrumentState state = effectiveInstrumentState(voiceIndex);
		enqueue(SrapProtocol.INSTRUMENT_STATE,
				encodeInstrumentState(requestId, voiceIndex, state.source(), state.instrument()), critical);
	}

	private InstrumentState effectiveInstrumentState(int voiceIndex) {
		SIDScoreIR.InstrumentIR override = instrumentOverrides.get(voiceIndex - 1);
		if (override != null) {
			return new InstrumentState(INSTRUMENT_SOURCE_OVERRIDE, override);
		}
		LoadedScore loaded = currentLoadedScore;
		if (loaded != null) {
			SIDScoreIR.TimedVoice voice = loaded.timedScore().voices().get(voiceIndex);
			if (voice != null && voice.instrument() != null) {
				return new InstrumentState(INSTRUMENT_SOURCE_SCORE, voice.instrument());
			}
		}
		return new InstrumentState(INSTRUMENT_SOURCE_DEFAULT, DEFAULT_SERVER_INSTRUMENT);
	}

	private SIDScoreIR.InstrumentIR effectivePlaybackInstrument(int voiceIndex, SIDScoreIR.InstrumentIR fallback) {
		if (!isValidVoiceIndex(voiceIndex)) {
			return fallback != null ? fallback : DEFAULT_SERVER_INSTRUMENT;
		}
		SIDScoreIR.InstrumentIR override = instrumentOverrides.get(voiceIndex - 1);
		if (override != null) {
			return override;
		}
		LoadedScore loaded = currentLoadedScore;
		if (loaded != null) {
			SIDScoreIR.TimedVoice voice = loaded.timedScore().voices().get(voiceIndex);
			if (voice != null && voice.instrument() != null) {
				return voice.instrument();
			}
		}
		return DEFAULT_SERVER_INSTRUMENT;
	}

	private byte[] encodeInstrumentState(long requestId, int voiceIndex, int source,
			SIDScoreIR.InstrumentIR instrument) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u32(requestId)
				.u8(voiceIndex)
				.u8(source)
				.u16(0);
		writeInstrumentFields(out, instrument);
		return out.toByteArray();
	}

	private static void writeInstrumentFields(SrapProtocol.PayloadWriter out, SIDScoreIR.InstrumentIR instrument) {
		int filterModeMask = instrument.filterModeMask() & 0x07;
		out.u8(instrument.waveMask() & 0x0F)
				.u8(clamp(instrument.adsr().a(), 0, 15))
				.u8(clamp(instrument.adsr().d(), 0, 15))
				.u8(clamp(instrument.adsr().s(), 0, 15))
				.u8(clamp(instrument.adsr().r(), 0, 15))
				.u16(clamp(instrument.pw().orElse(0x0800), 0, 0x0FFF))
				.i16(clamp(instrument.pwSweep(), -128, 128))
				.u16(clamp(instrument.pwMin().orElse(0x0000), 0, 0x0FFF))
				.u16(clamp(instrument.pwMax().orElse(0x0FFF), 0, 0x0FFF))
				.u8(filterModeMask)
				.u16(filterModeMask != 0 ? clamp(instrument.filterCutoff().orElse(0), 0, 0x07FF) : 0)
				.u8(filterModeMask != 0 ? clamp(instrument.filterRes().orElse(0), 0, 15) : 0)
				.u8(instrument.gateMode() == SIDScoreIR.InstrumentGateMode.LEGATO ? 1 : 0)
				.u8(clamp(instrument.gateMin(), 0, 16))
				.u8(instrument.sync() ? 1 : 0)
				.u8(instrument.ring() ? 1 : 0)
				.str(instrument.name());
	}

	private static int normalizeWaveMask(int waveMask) {
		if ((waveMask & SIDScoreIR.Wave.NOISE.mask) != 0) {
			return SIDScoreIR.Wave.NOISE.mask;
		}
		int nonNoise = waveMask & (SIDScoreIR.Wave.TRI.mask | SIDScoreIR.Wave.SAW.mask
				| SIDScoreIR.Wave.PULSE.mask);
		return nonNoise != 0 ? nonNoise : SIDScoreIR.Wave.PULSE.mask;
	}

	private static boolean isValidVoiceIndex(int voiceIndex) {
		return voiceIndex >= 1 && voiceIndex <= 3;
	}

	private static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	private byte[] encodeScoreMap(ScoreMapExporter.ScoreMap map) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u64(map.scoreId())
				.u16(map.sources().size());
		for (ScoreMapExporter.SourceEntry source : map.sources()) {
			out.u16(source.sourceId())
					.str(source.sourceUri())
					.str(source.sourcePath() != null ? source.sourcePath().toString() : "");
		}
		out.u32(map.events().size());
		for (ScoreMapExporter.EventEntry ev : map.events()) {
			out.i32(ev.eventId())
					.u8(ev.voiceIndex())
					.u8(ev.noteKind())
					.u16(ev.flags())
					.u64(ev.startFrame())
					.u64(ev.endFrame())
					.u16(ev.sourceId())
					.u32(ev.startLine())
					.u32(ev.startColumn())
					.u32(ev.endLine())
					.u32(ev.endColumn())
					.str(ev.displayText());
		}
		return out.toByteArray();
	}

	private byte[] encodeVoiceState(long scoreId, RealtimeAudioPlayer.PlaybackBlock block) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u64(scoreId)
				.u64(block.blockIndex())
				.u64(block.frameIndex())
				.f32(block.sampleRate());
		for (RealtimeAudioPlayer.VoiceSnapshot v : block.voices()) {
			out.u8(v.voiceIndex())
					.u8(v.noteKind())
					.u8(v.noteLetter())
					.i8(v.accidental())
					.i8(v.octave())
					.u8(v.waveMask())
					.u16(v.flags())
					.u16(v.freqReg())
					.u16(v.pulseWidth())
					.i8(v.pitchOffsetSemitones())
					.u8(0)
					.u8(0)
					.u8(0)
					.f32(v.envelopeLevel())
					.f32(v.outputLevel());
		}
		return out.toByteArray();
	}

	private void sendSilentVoiceState(long scoreId, boolean critical) {
		if ((clientCapabilities & SrapProtocol.CAP_VOICE_STATE) == 0) {
			return;
		}
		enqueue(SrapProtocol.VOICE_STATE, encodeSilentVoiceState(scoreId), critical);
	}

	private void sendSilentScopeState(long scoreId, boolean critical) {
		if ((clientCapabilities & SrapProtocol.CAP_SCOPE_BUCKETS) != 0) {
			enqueue(SrapProtocol.SCOPE_BUCKETS, encodeSilentScopeBuckets(scoreId), critical);
		}
		if ((clientCapabilities & SrapProtocol.CAP_SCOPE_SAMPLES) != 0) {
			enqueue(SrapProtocol.SCOPE_SAMPLES, encodeSilentScopeSamples(scoreId), critical);
		}
	}

	private byte[] encodeSilentVoiceState(long scoreId) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u64(scoreId)
				.u64(lastVoiceBlockIndex + 1)
				.u64(lastVoiceFrameIndex)
				.f32(lastVoiceSampleRate);
		for (int voice = 1; voice <= 3; voice++) {
			out.u8(voice)
					.u8(0)
					.u8(255)
					.i8(0)
					.i8(0)
					.u8(0)
					.u16(1 << 5)
					.u16(0)
					.u16(0x0800)
					.i8(0)
					.u8(0)
					.u8(0)
					.u8(0)
					.f32(0.0f)
					.f32(0.0f);
		}
		return out.toByteArray();
	}

	private byte[] encodeSilentScopeBuckets(long scoreId) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u64(scoreId)
				.u64(lastVoiceBlockIndex + 1)
				.f32(lastVoiceSampleRate)
				.u16(DEFAULT_SCOPE_BUCKETS)
				.u16(1);
		for (int voice = 1; voice <= 3; voice++) {
			out.u8(voice).u8(0);
			for (int b = 0; b < DEFAULT_SCOPE_BUCKETS; b++) {
				out.i16(0).i16(0);
			}
		}
		return out.toByteArray();
	}

	private byte[] encodeSilentScopeSamples(long scoreId) {
		int length = 512;
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u64(scoreId)
				.u64(lastVoiceBlockIndex + 1)
				.f32(lastVoiceSampleRate)
				.u16(length)
				.u16(0);
		for (int voice = 1; voice <= 3; voice++) {
			out.u8(voice).u8(0);
			for (int i = 0; i < length; i++) {
				out.i16(0);
			}
		}
		return out.toByteArray();
	}

	private byte[] encodeScopeBuckets(long scoreId, RealtimeAudioPlayer.PlaybackBlock block) {
		int bucketCount = Math.max(1, Math.min(DEFAULT_SCOPE_BUCKETS, block.length()));
		int samplesPerBucket = Math.max(1, block.length() / bucketCount);
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u64(scoreId)
				.u64(block.blockIndex())
				.f32(block.sampleRate())
				.u16(bucketCount)
				.u16(samplesPerBucket);
		for (int voice = 0; voice < 3; voice++) {
			out.u8(voice + 1).u8(0);
			float[] samples = block.samples()[voice];
			for (int b = 0; b < bucketCount; b++) {
				int start = b * block.length() / bucketCount;
				int end = Math.max(start + 1, (b + 1) * block.length() / bucketCount);
				end = Math.min(end, block.length());
				float min = 0.0f;
				float max = 0.0f;
				for (int i = start; i < end; i++) {
					float s = samples[i];
					if (i == start || s < min)
						min = s;
					if (i == start || s > max)
						max = s;
				}
				out.i16(floatToI16(min)).i16(floatToI16(max));
			}
		}
		return out.toByteArray();
	}

	private byte[] encodeScopeSamples(long scoreId, RealtimeAudioPlayer.PlaybackBlock block) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u64(scoreId)
				.u64(block.blockIndex())
				.f32(block.sampleRate())
				.u16(block.length())
				.u16(0);
		for (int voice = 0; voice < 3; voice++) {
			out.u8(voice + 1).u8(0);
			float[] samples = block.samples()[voice];
			for (int i = 0; i < block.length(); i++) {
				out.i16(floatToI16(samples[i]));
			}
		}
		return out.toByteArray();
	}

	private void sendPlaybackState(long requestId, int state, int reason, long scoreId, long frameIndex,
			long elapsedNanos, boolean critical) {
		playbackState = state;
		byte[] payload = SrapProtocol.payload()
				.u32(requestId)
				.u8(state)
				.u8(reason)
				.u16(0)
				.u64(scoreId)
				.u64(frameIndex)
				.u64(elapsedNanos)
				.toByteArray();
		enqueue(SrapProtocol.PLAYBACK_STATE, payload, critical);
	}

	private void sendHighlightState(long scoreId, long frameIndex, int v1, int v2, int v3, boolean critical) {
		byte[] payload = SrapProtocol.payload()
				.u64(scoreId)
				.u64(frameIndex)
				.i32(v1)
				.i32(v2)
				.i32(v3)
				.toByteArray();
		enqueue(SrapProtocol.HIGHLIGHT_STATE, payload, critical);
	}

	private void enqueueError(long requestId, int code, String message, boolean critical) {
		byte[] payload = SrapProtocol.payload()
				.u32(requestId)
				.u16(code)
				.u16(0)
				.str(message != null ? message : "")
				.toByteArray();
		enqueue(SrapProtocol.ERROR, payload, critical);
	}

	private boolean highlightChanged(int v1, int v2, int v3) {
		synchronized (lastHighlightIds) {
			if (lastHighlightIds[0] == v1 && lastHighlightIds[1] == v2 && lastHighlightIds[2] == v3) {
				return false;
			}
			lastHighlightIds[0] = v1;
			lastHighlightIds[1] = v2;
			lastHighlightIds[2] = v3;
			return true;
		}
	}

	private void resetHighlightIds() {
		synchronized (lastHighlightIds) {
			lastHighlightIds[0] = Integer.MIN_VALUE;
			lastHighlightIds[1] = Integer.MIN_VALUE;
			lastHighlightIds[2] = Integer.MIN_VALUE;
		}
	}

	private void enqueue(int type, byte[] payload, boolean critical) {
		if (!running) {
			return;
		}
		OutboundFrame frame = new OutboundFrame(type, payload);
		if (critical) {
			while (!outbound.offer(frame)) {
				outbound.poll();
			}
			return;
		}
		outbound.offer(frame);
	}

	private void writerLoop(Socket socket) {
		try {
			while (running || !outbound.isEmpty()) {
				OutboundFrame frame = outbound.take();
				if (frame.poison) {
					return;
				}
				SrapProtocol.writeFrame(socket.getOutputStream(), frame.type, 0, outboundSequence.getAndIncrement(),
						frame.payload);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException ignored) {
			running = false;
		}
	}

	private static int floatToI16(float value) {
		float clamped = Math.max(-1.0f, Math.min(1.0f, value));
		return Math.round(clamped * 32767.0f);
	}

	private record ParsedScore(SIDScoreParser.FileContext tree, SIDScoreIR.TimedScore timedScore) {
	}

	private record LoadedScore(String sourceUri, Path sourcePath, SIDScoreParser.FileContext tree,
			SIDScoreIR.TimedScore timedScore, SidModel sidModel) {
	}

	private record InstrumentState(int source, SIDScoreIR.InstrumentIR instrument) {
	}

	private record MidiVoiceAssignment(int voiceIndex, boolean enabled, String deviceSelector, int channel) {
		static MidiVoiceAssignment disabled(int voiceIndex) {
			return new MidiVoiceAssignment(voiceIndex, false, "", 1);
		}
	}

	private static final class ServerMidiSource implements RealtimeAudioPlayer.MidiSource, AutoCloseable {
		private final List<MidiInputRouter> routers;
		private final Map<Integer, MidiInputRouter> routersByVoice;
		private final String description;

		private ServerMidiSource(List<MidiInputRouter> routers, Map<Integer, MidiInputRouter> routersByVoice,
				String description) {
			this.routers = List.copyOf(routers);
			this.routersByVoice = Map.copyOf(routersByVoice);
			this.description = description;
		}

		static ServerMidiSource open(List<MidiVoiceAssignment> assignments) throws MidiUnavailableException {
			Map<String, Map<Integer, Integer>> channelsByDevice = new LinkedHashMap<>();
			for (MidiVoiceAssignment assignment : assignments) {
				if (!assignment.enabled()) {
					continue;
				}
				channelsByDevice.computeIfAbsent(assignment.deviceSelector(), ignored -> new LinkedHashMap<>())
						.put(assignment.voiceIndex(), assignment.channel());
			}
			List<MidiInputRouter> routers = new ArrayList<>();
			Map<Integer, MidiInputRouter> routersByVoice = new LinkedHashMap<>();
			List<String> descriptions = new ArrayList<>();
			try {
				for (var entry : channelsByDevice.entrySet()) {
					String selector = entry.getKey();
					Map<Integer, Integer> voiceMap = entry.getValue();
					logMidi("opening MIDI input selector='" + printableMidiSelector(selector) + "' voices="
							+ voiceMap);
					MidiInputRouter router = MidiInputRouter.open(selector, voiceMap,
							SIDScorePlayerServer::logMidiEventAsync);
					routers.add(router);
					descriptions.add("'" + router.deviceName() + "' voices=" + voiceMap);
					logMidi("opened MIDI input '" + router.deviceName() + "' selector='"
							+ printableMidiSelector(selector) + "' voices=" + voiceMap);
					for (int voiceIndex : voiceMap.keySet()) {
						routersByVoice.put(voiceIndex, router);
					}
				}
			} catch (MidiUnavailableException | RuntimeException e) {
				logMidi("failed to open MIDI input: " + e.getMessage());
				for (MidiInputRouter router : routers) {
					router.close();
				}
				throw e;
			}
			return new ServerMidiSource(routers, routersByVoice,
					descriptions.isEmpty() ? "none" : String.join("; ", descriptions));
		}

			String description() {
				return description;
			}

			boolean isOpen() {
				for (MidiInputRouter router : routers) {
					if (!router.isOpen()) {
						return false;
					}
				}
				return true;
			}

			void resumeInput() {
				for (MidiInputRouter router : routers) {
					router.resumeInput();
				}
			}

			@Override
			public boolean controlsVoice(int voiceIndex) {
			MidiInputRouter router = routersByVoice.get(voiceIndex);
			return router != null && router.controlsVoice(voiceIndex);
		}

		@Override
		public RealtimeAudioPlayer.MidiSnapshot snapshot(int voiceIndex) {
			MidiInputRouter router = routersByVoice.get(voiceIndex);
			return router != null ? router.snapshot(voiceIndex) : RealtimeAudioPlayer.MidiSnapshot.off();
		}

		@Override
		public List<RealtimeAudioPlayer.MidiEvent> drainEvents(int voiceIndex) {
			MidiInputRouter router = routersByVoice.get(voiceIndex);
			return router != null ? router.drainEvents(voiceIndex) : List.of();
		}

		@Override
		public void close() {
			for (MidiInputRouter router : routers) {
				logMidi("closing MIDI input '" + router.deviceName() + "'");
				router.close();
			}
		}
	}

	private record OutboundFrame(int type, byte[] payload, boolean poison) {
		OutboundFrame(int type, byte[] payload) {
			this(type, payload, false);
		}

		static OutboundFrame poisonFrame() {
			return new OutboundFrame(-1, new byte[0], true);
		}
	}

	private static final class ThrowingErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			throw new ScoreBuildingListener.ValidationException(line, charPositionInLine, msg);
		}
	}
}
