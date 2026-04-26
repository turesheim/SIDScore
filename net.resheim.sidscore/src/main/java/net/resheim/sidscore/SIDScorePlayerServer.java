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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

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
import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;
import net.resheim.sidscore.server.ScoreMapExporter;
import net.resheim.sidscore.server.SrapProtocol;
import net.resheim.sidscore.sid.SidModel;

public final class SIDScorePlayerServer {
	private static final int DEFAULT_SCOPE_BUCKETS = 64;
	private static final int OUTBOUND_QUEUE_SIZE = 512;

	private final int requestedPort;
	private final BlockingQueue<OutboundFrame> outbound = new ArrayBlockingQueue<>(OUTBOUND_QUEUE_SIZE);
	private final AtomicLong outboundSequence = new AtomicLong(1);
	private final AtomicLong scoreIds = new AtomicLong(1);

	private volatile boolean running = true;
	private volatile int clientCapabilities = SrapProtocol.CAP_ALL;
	private volatile int playbackState = SrapProtocol.STATE_IDLE;
	private volatile long currentScoreId = 0;
	private volatile ScoreMapExporter.ScoreMap currentScoreMap = null;
	private volatile RealtimeAudioPlayer currentPlayer = null;
	private volatile Thread currentPlayerThread = null;
	private volatile boolean stopRequestedByClient = false;
	private final int[] lastHighlightIds = { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };

	public SIDScorePlayerServer(int requestedPort) {
		this.requestedPort = requestedPort;
	}

	public static void main(String[] args) throws Exception {
		int port = 0;
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "--port" -> {
				if (i + 1 >= args.length) {
					throw new IllegalArgumentException("--port requires a value");
				}
				port = Integer.parseInt(args[++i]);
			}
			default -> throw new IllegalArgumentException("Unknown player server option: " + args[i]);
			}
		}
		new SIDScorePlayerServer(port).run();
	}

	public void run() throws Exception {
		InetAddress loopback = InetAddress.getByName("127.0.0.1");
		try (ServerSocket server = new ServerSocket(requestedPort, 1, loopback)) {
			int port = server.getLocalPort();
			System.out.println("{\"event\":\"ready\",\"protocol\":\"srap-server\",\"version\":1,\"port\":" + port
					+ "}");
			System.out.flush();
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
		clientCapabilities = caps & SrapProtocol.CAP_ALL;
		byte[] payload = SrapProtocol.payload()
				.u16(SrapProtocol.VERSION)
				.u32(SrapProtocol.CAP_ALL)
				.str("SIDScore Player Server")
				.toByteArray();
		enqueue(SrapProtocol.HELLO_ACK, payload, true);
	}

	private void handleCommand(SrapProtocol.Frame frame) {
		switch (frame.type()) {
		case SrapProtocol.PLAY -> handlePlay(frame.payload());
		case SrapProtocol.PLAY_SOURCE -> handlePlaySource(frame.payload());
		case SrapProtocol.PAUSE -> handlePause(frame.payload());
		case SrapProtocol.CONTINUE -> handleContinue(frame.payload());
		case SrapProtocol.STOP -> handleStop(frame.payload());
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
		stopRequestedByClient = false;
		resetHighlightIds();
		sendPlaybackState(requestId, SrapProtocol.STATE_LOADING, SrapProtocol.REASON_CLIENT_REQUEST, scoreId, 0, 0,
				true);

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

		ScoreMapExporter.ScoreMap scoreMap = ScoreMapExporter.build(scoreId, parsed.tree(), parsed.timedScore(),
				sourceUri, sourcePath);
		currentScoreMap = scoreMap;
		if ((clientCapabilities & SrapProtocol.CAP_SCORE_MAP) != 0) {
			enqueue(SrapProtocol.SCORE_MAP, encodeScoreMap(scoreMap), true);
		}

		SidModel sidModel = switch (sidModelRaw) {
		case 2 -> SidModel.MOS8580;
		default -> SidModel.MOS6581;
		};
		RealtimeAudioPlayer player = new RealtimeAudioPlayer(sidModel);
		currentPlayer = player;
		Thread thread = new Thread(() -> runPlayer(requestId, scoreId, player, parsed.timedScore()),
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
		stopRequestedByClient = true;
		stopCurrent(requestId, true);
	}

	private void runPlayer(long requestId, long scoreId, RealtimeAudioPlayer player, SIDScoreIR.TimedScore timed) {
		try {
			sendPlaybackState(requestId, SrapProtocol.STATE_PLAYING, SrapProtocol.REASON_CLIENT_REQUEST, scoreId, 0, 0,
					true);
			player.playWithTelemetry(timed, block -> handlePlaybackBlock(scoreId, block));
			if (!stopRequestedByClient && running && currentScoreId == scoreId) {
				sendHighlightState(scoreId, 0, -1, -1, -1, true);
				sendPlaybackState(0, SrapProtocol.STATE_ENDED, SrapProtocol.REASON_END_OF_SCORE, scoreId, 0, 0, true);
			}
		} catch (LineUnavailableException e) {
			sendPlaybackState(requestId, SrapProtocol.STATE_ERROR, SrapProtocol.REASON_PLAYBACK_ERROR, scoreId, 0, 0,
					true);
			enqueueError(requestId, SrapProtocol.ERR_PLAYBACK_ERROR, e.getMessage(), true);
		} finally {
			if (currentPlayer == player) {
				currentPlayer = null;
				currentPlayerThread = null;
			}
		}
	}

	private void handlePlaybackBlock(long scoreId, RealtimeAudioPlayer.PlaybackBlock block) {
		if (!running || currentScoreId != scoreId) {
			return;
		}
		if ((clientCapabilities & SrapProtocol.CAP_VOICE_STATE) != 0) {
			enqueue(SrapProtocol.VOICE_STATE, encodeVoiceState(scoreId, block), false);
		}
		if ((clientCapabilities & SrapProtocol.CAP_SCOPE_BUCKETS) != 0) {
			enqueue(SrapProtocol.SCOPE_BUCKETS, encodeScopeBuckets(scoreId, block), false);
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

	private void stopCurrent(long requestId, boolean emitState) {
		RealtimeAudioPlayer player = currentPlayer;
		if (player != null) {
			player.stop();
		}
		Thread thread = currentPlayerThread;
		if (thread != null && thread != Thread.currentThread()) {
			try {
				thread.join(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		currentPlayer = null;
		currentPlayerThread = null;
		resetHighlightIds();
		if (emitState) {
			sendHighlightState(currentScoreId, 0, -1, -1, -1, true);
			sendPlaybackState(requestId, SrapProtocol.STATE_STOPPED, SrapProtocol.REASON_CLIENT_REQUEST, currentScoreId,
					0, 0, true);
		}
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
