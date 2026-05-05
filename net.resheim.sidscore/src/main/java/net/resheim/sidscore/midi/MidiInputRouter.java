/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Torkild Ulvøy Resheim <torkildr@gmail.com> - initial API and implementation
 */
package net.resheim.sidscore.midi;

import net.resheim.sidscore.ir.RealtimeAudioPlayer;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Routes live MIDI channel input to SID voices. Multiple voices may share one
 * MIDI channel; incoming notes are then allocated across those SID voices.
 */
public final class MidiInputRouter implements RealtimeAudioPlayer.MidiSource, AutoCloseable {

	public static final double DEFAULT_PITCH_BEND_RANGE = 2.0;
	private static final String AWT_EVENT_PUMP_PROPERTY = "sidscore.midi.awtEventPump";
	private static final AtomicBoolean NATIVE_EVENT_PUMP_INITIALIZED = new AtomicBoolean(false);
	private static final DeviceProvider SYSTEM_DEVICE_PROVIDER = new DeviceProvider() {
		@Override
		public MidiDevice.Info[] getMidiDeviceInfo() {
			return MidiSystem.getMidiDeviceInfo();
		}

		@Override
		public MidiDevice getMidiDevice(MidiDevice.Info info) throws MidiUnavailableException {
			return MidiSystem.getMidiDevice(info);
		}
	};
	private static volatile DeviceProvider deviceProvider = SYSTEM_DEVICE_PROVIDER;

	private final MidiDevice device;
	private final Transmitter transmitter;
	private final EventListener eventListener;
	private volatile Map<Integer, List<Integer>> voicesByChannel;
	private volatile Map<Integer, Integer> voiceChannelMap;
	private final VoiceSlot[] slots = new VoiceSlot[4];
	private final double[] pitchBendByChannel = new double[17];
	private long sequence = 0;
	private volatile boolean closed = false;
	private volatile boolean suspended = false;

	private MidiInputRouter(MidiDevice device, Transmitter transmitter, Map<Integer, Integer> voiceChannelMap,
			EventListener eventListener) {
		this.device = device;
		this.transmitter = transmitter;
		this.eventListener = eventListener;
		this.voiceChannelMap = Collections.unmodifiableMap(new LinkedHashMap<>(voiceChannelMap));
		this.voicesByChannel = Collections.unmodifiableMap(groupVoicesByChannel(voiceChannelMap));
		for (int i = 1; i <= 3; i++) {
			slots[i] = new VoiceSlot();
		}
	}

	public static MidiInputRouter open(String selector, Map<Integer, Integer> voiceChannelMap)
			throws MidiUnavailableException {
		return open(selector, voiceChannelMap, null);
	}

	public static MidiInputRouter open(String selector, Map<Integer, Integer> voiceChannelMap,
			EventListener eventListener) throws MidiUnavailableException {
		Map<Integer, Integer> validatedMap = validateVoiceChannelMap(voiceChannelMap);
		ensureNativeMidiEventPump(eventListener);
		List<InputDevice> devices = listInputDevices();
		emit(eventListener, "input scan before open found " + devices.size() + " device(s): "
				+ describeInputDevices(devices));
		if (devices.isEmpty()) {
			throw new MidiUnavailableException("No MIDI input devices found.");
		}

		InputDevice selected = selectDevice(devices, selector)
				.orElseThrow(() -> new MidiUnavailableException("No MIDI input device matches: " + selector));
		MidiDevice device = deviceProvider.getMidiDevice(selected.info());
		device.open();
		Transmitter transmitter = null;
		try {
			transmitter = device.getTransmitter();
			MidiInputRouter router = new MidiInputRouter(device, transmitter, validatedMap, eventListener);
			transmitter.setReceiver(router.new RoutingReceiver());
			router.emit("RECEIVER attached to " + router.deviceName() + " (" + selected.debugDescription()
					+ ") map " + validatedMap);
			router.emit("input open complete on thread '" + Thread.currentThread().getName() + "'");
			return router;
		} catch (MidiUnavailableException | RuntimeException e) {
			if (transmitter != null) {
				transmitter.close();
			}
			device.close();
			throw e;
		}
	}

	public static List<InputDevice> listInputDevices() {
		List<InputDevice> devices = new ArrayList<>();
		MidiDevice.Info[] infos = deviceProvider.getMidiDeviceInfo();
		for (MidiDevice.Info info : infos) {
			try {
				MidiDevice device = deviceProvider.getMidiDevice(info);
				if (isUsableInput(device)) {
					devices.add(new InputDevice(devices.size(), info));
				}
			} catch (MidiUnavailableException ignored) {
				// A device that cannot be instantiated is not useful for live input.
			}
		}
		return List.copyOf(devices);
	}

	/**
	 * Installs a deterministic Java Sound device provider for tests.
	 * <p>
	 * Production code always uses {@link MidiSystem}; tests need this seam because
	 * build machines usually have no physical MIDI input attached. The returned
	 * closeable restores the previous provider, so each test can keep its mocked
	 * instrument isolated from the next one.
	 */
	static AutoCloseable useDeviceProviderForTesting(DeviceProvider provider) {
		DeviceProvider previous = deviceProvider;
		deviceProvider = provider != null ? provider : SYSTEM_DEVICE_PROVIDER;
		return () -> deviceProvider = previous;
	}

	public static Map<Integer, Integer> defaultVoiceChannelMap() {
		Map<Integer, Integer> map = new LinkedHashMap<>();
		map.put(1, 1);
		return Collections.unmodifiableMap(map);
	}

	public static Map<Integer, Integer> parseVoiceChannelMap(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("MIDI map must not be empty");
		}
		Map<Integer, Integer> map = new LinkedHashMap<>();
		for (String item : raw.split(",")) {
			String trimmed = item.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			String[] pair = trimmed.split("[:=]", 2);
			if (pair.length != 2) {
				throw new IllegalArgumentException("MIDI map entries must use voice:channel, got: " + trimmed);
			}
			int voice = parseInt(pair[0].trim(), "voice", trimmed);
			int channel = parseInt(pair[1].trim(), "channel", trimmed);
			if (voice < 1 || voice > 3) {
				throw new IllegalArgumentException("MIDI voice must be 1..3, got " + voice);
			}
			if (channel < 1 || channel > 16) {
				throw new IllegalArgumentException("MIDI channel must be 1..16, got " + channel);
			}
			if (map.put(voice, channel) != null) {
				throw new IllegalArgumentException("MIDI voice appears more than once: " + voice);
			}
		}
		if (map.isEmpty()) {
			throw new IllegalArgumentException("MIDI map must contain at least one voice:channel entry");
		}
		return Collections.unmodifiableMap(map);
	}

	public static void initializeNativeMidiEventPump(EventListener eventListener) {
		ensureNativeMidiEventPump(eventListener);
	}

	public static String nativeMidiEventPumpState() {
		return "os='" + System.getProperty("os.name", "") + "'"
				+ ", awtPump='" + System.getProperty(AWT_EVENT_PUMP_PROPERTY, "") + "'"
				+ ", awtPumpDisabled='" + System.getProperty(AWT_EVENT_PUMP_PROPERTY + ".disabled", "") + "'"
				+ ", java.awt.headless='" + System.getProperty("java.awt.headless", "") + "'"
				+ ", graphicsHeadless=" + GraphicsEnvironment.isHeadless()
				+ ", apple.awt.UIElement='" + System.getProperty("apple.awt.UIElement", "") + "'"
				+ ", initialized=" + NATIVE_EVENT_PUMP_INITIALIZED.get();
	}

	public Map<Integer, Integer> voiceChannelMap() {
		return voiceChannelMap;
	}

	public String deviceName() {
		return displayName(device.getDeviceInfo());
	}

	public boolean isOpen() {
		return !closed && device.isOpen();
	}

	public void suspendInput() {
		synchronized (this) {
			if (closed) {
				return;
			}
			suspended = true;
			clearState();
		}
	}

	public void resumeInput() {
		synchronized (this) {
			if (closed) {
				return;
			}
			if (!suspended) {
				return;
			}
			clearState();
			suspended = false;
		}
	}

	/**
	 * Updates the voice-to-channel routing without closing the Java Sound device.
	 * <p>
	 * macOS/CoreMIDI can report a reopened input device as active before callbacks
	 * are actually delivered again. Server-side MIDI settings changes therefore
	 * remap an already-open router when the selected device is unchanged. Existing
	 * voice state is preserved for voices that keep the same channel so a held key
	 * is still visible after the audio monitor restarts.
	 */
	public void remapVoiceChannels(Map<Integer, Integer> updatedVoiceChannelMap) {
		Map<Integer, Integer> validatedMap = validateVoiceChannelMap(updatedVoiceChannelMap);
		synchronized (this) {
			if (closed) {
				return;
			}
			Map<Integer, Integer> previousMap = voiceChannelMap;
			for (int voiceIndex = 1; voiceIndex <= 3; voiceIndex++) {
				Integer previousChannel = previousMap.get(voiceIndex);
				Integer updatedChannel = validatedMap.get(voiceIndex);
				if (previousChannel == null ? updatedChannel != null : !previousChannel.equals(updatedChannel)) {
					slots[voiceIndex].clear();
				}
			}
			voiceChannelMap = validatedMap;
			voicesByChannel = groupVoicesByChannel(validatedMap);
		}
	}

	@Override
	public boolean controlsVoice(int voiceIndex) {
		return voiceChannelMap.containsKey(voiceIndex);
	}

	@Override
	public RealtimeAudioPlayer.MidiSnapshot snapshot(int voiceIndex) {
		if (voiceIndex < 1 || voiceIndex > 3 || !controlsVoice(voiceIndex)) {
			return RealtimeAudioPlayer.MidiSnapshot.off();
		}
		return slots[voiceIndex].snapshot();
	}

	@Override
	public List<RealtimeAudioPlayer.MidiEvent> drainEvents(int voiceIndex) {
		if (voiceIndex < 1 || voiceIndex > 3 || !controlsVoice(voiceIndex)) {
			return List.of();
		}
		return slots[voiceIndex].drainEvents();
	}

	@Override
	public void close() {
		synchronized (this) {
			if (closed) {
				return;
			}
			closed = true;
			for (int i = 1; i <= 3; i++) {
				slots[i].release(++sequence);
			}
		}
		transmitter.close();
		device.close();
	}

	private void clearState() {
		Arrays.fill(pitchBendByChannel, 0.0);
		for (int i = 1; i <= 3; i++) {
			slots[i].clear();
		}
	}

	private synchronized void noteOn(int channel, int note, int velocity) {
		if (closed || suspended) {
			return;
		}
		if (velocity <= 0) {
			noteOff(channel, note);
			return;
		}
		List<Integer> voices = voicesByChannel.get(channel);
		if (voices == null || voices.isEmpty()) {
			emit("NOTE ON ch " + channel + " note " + note + " vel " + velocity + " (unmapped)");
			return;
		}
		int voice = findVoicePlaying(voices, note);
		if (voice < 0) {
			voice = findFreeVoice(voices);
		}
		if (voice < 0) {
			voice = findOldestVoice(voices);
		}
		if (voice < 0) {
			emit("NOTE ON ch " + channel + " note " + note + " vel " + velocity + " (unmapped)");
			return;
		}
		slots[voice].start(note, velocity, pitchBendByChannel[channel], ++sequence);
		emit("NOTE ON ch " + channel + " note " + note + " vel " + velocity + " -> voice " + voice);
	}

	private synchronized void noteOff(int channel, int note) {
		if (closed || suspended) {
			return;
		}
		List<Integer> voices = voicesByChannel.get(channel);
		if (voices == null) {
			emit("NOTE OFF ch " + channel + " note " + note + " (unmapped)");
			return;
		}
		for (int voice : voices) {
			if (slots[voice].gate && slots[voice].note == note) {
				slots[voice].release(++sequence);
				emit("NOTE OFF ch " + channel + " note " + note + " <- voice " + voice);
			}
		}
	}

	private synchronized void allNotesOff(int channel) {
		if (closed || suspended) {
			return;
		}
		List<Integer> voices = voicesByChannel.get(channel);
		if (voices == null) {
			return;
		}
		for (int voice : voices) {
			slots[voice].release(++sequence);
		}
		emit("ALL NOTES OFF ch " + channel);
	}

	private synchronized void pitchBend(int channel, int lsb, int msb) {
		if (closed || suspended) {
			return;
		}
		int value = (lsb & 0x7F) | ((msb & 0x7F) << 7);
		double normalized = (value - 8192) / 8192.0;
		double semitones = Math.max(-1.0, Math.min(1.0, normalized)) * DEFAULT_PITCH_BEND_RANGE;
		pitchBendByChannel[channel] = semitones;
		List<Integer> voices = voicesByChannel.get(channel);
		if (voices == null) {
			return;
		}
		for (int voice : voices) {
			slots[voice].pitchBendSemitones = semitones;
		}
		emit("PITCH BEND ch " + channel + " value " + value + " (" + String.format(Locale.ROOT, "%.2f", semitones)
				+ " st)");
	}

	private void controlChange(int channel, int controller, int value) {
		if (closed || suspended) {
			return;
		}
		if (controller == 120 || controller == 123) {
			allNotesOff(channel);
		} else {
			emit("CC ch " + channel + " #" + controller + " = " + value);
		}
	}

	private void emit(String message) {
		EventListener listener = eventListener;
		if (listener != null) {
			listener.onMidiEvent(message);
		}
	}

	private static void emit(EventListener listener, String message) {
		if (listener != null) {
			listener.onMidiEvent(message);
		}
	}

	private int findVoicePlaying(List<Integer> voices, int note) {
		for (int voice : voices) {
			if (slots[voice].gate && slots[voice].note == note) {
				return voice;
			}
		}
		return -1;
	}

	private int findFreeVoice(List<Integer> voices) {
		for (int voice : voices) {
			if (!slots[voice].gate) {
				return voice;
			}
		}
		return -1;
	}

	private int findOldestVoice(List<Integer> voices) {
		int oldestVoice = -1;
		long oldestSequence = Long.MAX_VALUE;
		for (int voice : voices) {
			if (slots[voice].sequence < oldestSequence) {
				oldestSequence = slots[voice].sequence;
				oldestVoice = voice;
			}
		}
		return oldestVoice;
	}

	private static boolean isUsableInput(MidiDevice device) {
		if (device instanceof Sequencer || device instanceof Synthesizer) {
			return false;
		}
		int maxTransmitters = device.getMaxTransmitters();
		return maxTransmitters != 0;
	}

	private static String describeInputDevices(List<InputDevice> devices) {
		if (devices.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < devices.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			InputDevice device = devices.get(i);
			sb.append(device.debugDescription());
		}
		sb.append(']');
		return sb.toString();
	}

	private static Optional<InputDevice> selectDevice(List<InputDevice> devices, String selector) {
		if (selector == null || selector.isBlank()) {
			return devices.stream().findFirst();
		}
		String trimmed = selector.trim();
		try {
			int index = Integer.parseInt(trimmed);
			return devices.stream().filter(d -> d.index() == index).findFirst();
		} catch (NumberFormatException ignored) {
			// Select by name/vendor/description below.
		}

		String needle = trimmed.toLowerCase(Locale.ROOT);
		return devices.stream()
				.filter(d -> d.searchText().contains(needle))
				.findFirst();
	}

	private static Map<Integer, Integer> validateVoiceChannelMap(Map<Integer, Integer> raw) {
		Map<Integer, Integer> map = new LinkedHashMap<>();
		Map<Integer, Integer> source = raw == null || raw.isEmpty() ? defaultVoiceChannelMap() : raw;
		for (var entry : source.entrySet()) {
			int voice = entry.getKey();
			int channel = entry.getValue();
			if (voice < 1 || voice > 3) {
				throw new IllegalArgumentException("MIDI voice must be 1..3, got " + voice);
			}
			if (channel < 1 || channel > 16) {
				throw new IllegalArgumentException("MIDI channel must be 1..16, got " + channel);
			}
			map.put(voice, channel);
		}
		if (map.isEmpty()) {
			throw new IllegalArgumentException("MIDI map must contain at least one voice");
		}
		return Collections.unmodifiableMap(map);
	}

	private static Map<Integer, List<Integer>> groupVoicesByChannel(Map<Integer, Integer> voiceChannelMap) {
		Map<Integer, List<Integer>> grouped = new LinkedHashMap<>();
		for (var entry : voiceChannelMap.entrySet()) {
			grouped.computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>()).add(entry.getKey());
		}
		for (var entry : grouped.entrySet()) {
			entry.setValue(List.copyOf(entry.getValue()));
		}
		return grouped;
	}

	private static int parseInt(String raw, String label, String entry) {
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid MIDI " + label + " in map entry '" + entry + "'", e);
		}
	}

	private static void ensureNativeMidiEventPump(EventListener eventListener) {
		String configured = System.getProperty(AWT_EVENT_PUMP_PROPERTY, "");
		boolean disabled = "false".equalsIgnoreCase(configured)
				|| Boolean.getBoolean(AWT_EVENT_PUMP_PROPERTY + ".disabled");
		if (disabled) {
			emit(eventListener, "macOS MIDI event pump disabled; " + nativeMidiEventPumpState());
			return;
		}
		if (!isMacOS()) {
			return;
		}
		try {
			if (System.getProperty("apple.awt.UIElement") == null) {
				System.setProperty("apple.awt.UIElement", "true");
			}
			if (GraphicsEnvironment.isHeadless()) {
				emit(eventListener, "macOS MIDI event pump skipped; " + nativeMidiEventPumpState());
				return;
			}
			if (!NATIVE_EVENT_PUMP_INITIALIZED.compareAndSet(false, true)) {
				emit(eventListener, "macOS MIDI event pump already initialized; " + nativeMidiEventPumpState());
				return;
			}
			Toolkit.getDefaultToolkit();
			EventQueue.invokeLater(() -> {
				// Keep the AWT/AppKit event infrastructure alive for native MIDI callbacks.
			});
			emit(eventListener, "macOS MIDI event pump initialized; " + nativeMidiEventPumpState());
		} catch (Throwable e) {
			emit(eventListener, "macOS MIDI event pump unavailable: " + e.getClass().getSimpleName()
					+ ": " + e.getMessage() + "; " + nativeMidiEventPumpState());
		}
	}

	private static boolean isMacOS() {
		return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
	}

	private final class RoutingReceiver implements Receiver {
		@Override
		public void send(MidiMessage message, long timeStamp) {
			try {
				if (closed || suspended) {
					return;
				}
				if (!(message instanceof ShortMessage shortMessage)) {
					routeRawMessage(message);
					return;
				}
				int channel = shortMessage.getChannel() + 1;
				emitReceivedMessage(shortMessage.getCommand(), channel, shortMessage.getData1(), shortMessage.getData2(),
						message.getClass().getName());
				routeShortMessage(shortMessage.getCommand(), channel, shortMessage.getData1(),
						shortMessage.getData2());
			} catch (RuntimeException e) {
				emit("ERROR routing MIDI message: " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		private void routeRawMessage(MidiMessage message) {
			byte[] data = message.getMessage();
			int length = message.getLength();
			if (length <= 0 || data.length == 0) {
				emit("RAW empty MIDI message from " + message.getClass().getName());
				return;
			}
			int status = data[0] & 0xFF;
			int command = status & 0xF0;
			int channel = (status & 0x0F) + 1;
			int data1 = length > 1 ? data[1] & 0x7F : 0;
			int data2 = length > 2 ? data[2] & 0x7F : 0;
			if (command >= 0x80 && command <= 0xE0 && length >= 3) {
				emitReceivedMessage(command, channel, data1, data2, message.getClass().getName());
				routeShortMessage(command, channel, data1, data2);
				return;
			}
			if (!isNoisySystemMessage(status)) {
				emit("RAW status 0x" + Integer.toHexString(status) + " len " + length + " bytes "
						+ hexBytes(data, length) + " from " + message.getClass().getName());
			}
		}

		private void routeShortMessage(int command, int channel, int data1, int data2) {
			switch (command) {
			case ShortMessage.NOTE_ON -> noteOn(channel, data1, data2);
			case ShortMessage.NOTE_OFF -> noteOff(channel, data1);
			case ShortMessage.PITCH_BEND -> pitchBend(channel, data1, data2);
			case ShortMessage.CONTROL_CHANGE -> controlChange(channel, data1, data2);
			default -> {
				emit("CMD 0x" + Integer.toHexString(command) + " ch " + channel + " data " + data1 + "," + data2);
			}
			}
		}

		private void emitReceivedMessage(int command, int channel, int data1, int data2, String sourceClass) {
			emit("RX " + commandName(command) + " ch " + channel + " data " + data1 + "," + data2
					+ " from " + sourceClass);
		}

		private String commandName(int command) {
			return switch (command) {
			case ShortMessage.NOTE_ON -> "NOTE_ON";
			case ShortMessage.NOTE_OFF -> "NOTE_OFF";
			case ShortMessage.PITCH_BEND -> "PITCH_BEND";
			case ShortMessage.CONTROL_CHANGE -> "CONTROL_CHANGE";
			case ShortMessage.PROGRAM_CHANGE -> "PROGRAM_CHANGE";
			case ShortMessage.CHANNEL_PRESSURE -> "CHANNEL_PRESSURE";
			case ShortMessage.POLY_PRESSURE -> "POLY_PRESSURE";
			default -> "CMD_0x" + Integer.toHexString(command);
			};
		}

		private boolean isNoisySystemMessage(int status) {
			return status == 0xF8 || status == 0xFE;
		}

		private String hexBytes(byte[] data, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < length && i < data.length; i++) {
				if (i > 0) {
					sb.append(' ');
				}
				sb.append(String.format(Locale.ROOT, "%02X", data[i] & 0xFF));
			}
			return sb.toString();
		}

		@Override
		public void close() {
			MidiInputRouter.this.close();
		}
	}

	private static final class VoiceSlot {
		volatile int note = -1;
		volatile int velocity = 0;
		volatile boolean gate = false;
		volatile double pitchBendSemitones = 0.0;
		volatile long sequence = 0;
		volatile long noteOnId = 0;
		volatile long noteOffId = 0;
		volatile int lastNote = -1;
		volatile int lastVelocity = 0;
		final Queue<RealtimeAudioPlayer.MidiEvent> events = new ConcurrentLinkedDeque<>();

		void start(int note, int velocity, double pitchBendSemitones, long sequence) {
			this.note = Math.max(0, Math.min(127, note));
			this.velocity = Math.max(1, Math.min(127, velocity));
			this.pitchBendSemitones = pitchBendSemitones;
			this.sequence = sequence;
			this.noteOnId = sequence;
			this.lastNote = this.note;
			this.lastVelocity = this.velocity;
			this.gate = true;
			events.offer(new RealtimeAudioPlayer.MidiEvent(this.note, this.velocity, true, pitchBendSemitones,
					sequence));
		}

		void release(long sequence) {
			if (!gate && noteOffId >= noteOnId) {
				return;
			}
			this.sequence = sequence;
			this.noteOffId = sequence;
			if (note >= 0) {
				this.lastNote = note;
			}
			if (velocity > 0) {
				this.lastVelocity = velocity;
			}
			events.offer(new RealtimeAudioPlayer.MidiEvent(this.lastNote, this.lastVelocity, false,
					pitchBendSemitones, sequence));
			this.velocity = 0;
			this.gate = false;
		}

		void clear() {
			note = -1;
			velocity = 0;
			gate = false;
			pitchBendSemitones = 0.0;
			sequence = 0;
			noteOnId = 0;
			noteOffId = 0;
			lastNote = -1;
			lastVelocity = 0;
			events.clear();
		}

		RealtimeAudioPlayer.MidiSnapshot snapshot() {
			long onId = noteOnId;
			long offId = noteOffId;
			boolean snapshotGate = gate;
			int currentNote = note;
			int previousNote = lastNote;
			int currentVelocity = velocity;
			int previousVelocity = lastVelocity;
			double bend = pitchBendSemitones;
			if (onId == 0 || (currentNote < 0 && previousNote < 0)) {
				return RealtimeAudioPlayer.MidiSnapshot.off();
			}
			int snapshotNote = snapshotGate ? currentNote : previousNote;
			int snapshotVelocity = snapshotGate ? currentVelocity : previousVelocity;
			return new RealtimeAudioPlayer.MidiSnapshot(snapshotNote, Math.max(0, snapshotVelocity), snapshotGate, bend,
					onId, offId);
		}

		List<RealtimeAudioPlayer.MidiEvent> drainEvents() {
			if (events.isEmpty()) {
				return List.of();
			}
			List<RealtimeAudioPlayer.MidiEvent> drained = new ArrayList<>();
			RealtimeAudioPlayer.MidiEvent event;
			while ((event = events.poll()) != null) {
				drained.add(event);
			}
			return drained;
		}
	}

	public static final class InputDevice {
		private final int index;
		private final MidiDevice.Info info;

		private InputDevice(int index, MidiDevice.Info info) {
			this.index = index;
			this.info = info;
		}

		public int index() {
			return index;
		}

		public String name() {
			return info.getName();
		}

		public String displayName() {
			return MidiInputRouter.displayName(info);
		}

		public String vendor() {
			return info.getVendor();
		}

		public String description() {
			return info.getDescription();
		}

		public String version() {
			return info.getVersion();
		}

		private MidiDevice.Info info() {
			return info;
		}

		private String searchText() {
			return String.join(" ", Arrays.asList(name(), displayName(), vendor(), description(), version()))
					.toLowerCase(Locale.ROOT);
		}

		private String debugDescription() {
			return "index=" + index + ", name='" + name() + "', vendor='" + vendor()
					+ "', description='" + description() + "', version='" + version() + "'";
		}
	}

	private static String displayName(MidiDevice.Info info) {
		String name = cleanInfoValue(info.getName());
		if (!name.isEmpty() && !name.toLowerCase(Locale.ROOT).startsWith("unknown")) {
			return name;
		}
		String description = cleanInfoValue(info.getDescription());
		if (!description.isEmpty() && !description.toLowerCase(Locale.ROOT).startsWith("unknown")) {
			return description;
		}
		return name.isEmpty() ? "Unknown MIDI input" : name;
	}

	private static String cleanInfoValue(String value) {
		return value == null ? "" : value.trim();
	}

	@FunctionalInterface
	public interface EventListener {
		void onMidiEvent(String message);
	}

	interface DeviceProvider {
		MidiDevice.Info[] getMidiDeviceInfo();

		MidiDevice getMidiDevice(MidiDevice.Info info) throws MidiUnavailableException;
	}
}
