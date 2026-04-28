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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Routes live MIDI channel input to SID voices. Multiple voices may share one
 * MIDI channel; incoming notes are then allocated across those SID voices.
 */
public final class MidiInputRouter implements RealtimeAudioPlayer.MidiSource, AutoCloseable {

	public static final double DEFAULT_PITCH_BEND_RANGE = 2.0;

	private final MidiDevice device;
	private final Transmitter transmitter;
	private final EventListener eventListener;
	private final Map<Integer, List<Integer>> voicesByChannel;
	private final Map<Integer, Integer> voiceChannelMap;
	private final VoiceSlot[] slots = new VoiceSlot[4];
	private final double[] pitchBendByChannel = new double[17];
	private long sequence = 0;
	private volatile boolean closed = false;

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
		List<InputDevice> devices = listInputDevices();
		if (devices.isEmpty()) {
			throw new MidiUnavailableException("No MIDI input devices found.");
		}

		InputDevice selected = selectDevice(devices, selector)
				.orElseThrow(() -> new MidiUnavailableException("No MIDI input device matches: " + selector));
		MidiDevice device = MidiSystem.getMidiDevice(selected.info());
		device.open();
		Transmitter transmitter = null;
		try {
			transmitter = device.getTransmitter();
			MidiInputRouter router = new MidiInputRouter(device, transmitter, validatedMap, eventListener);
			transmitter.setReceiver(router.new RoutingReceiver());
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
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info info : infos) {
			try {
				MidiDevice device = MidiSystem.getMidiDevice(info);
				if (isUsableInput(device)) {
					devices.add(new InputDevice(devices.size(), info));
				}
			} catch (MidiUnavailableException ignored) {
				// A device that cannot be instantiated is not useful for live input.
			}
		}
		return List.copyOf(devices);
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

	public Map<Integer, Integer> voiceChannelMap() {
		return voiceChannelMap;
	}

	public String deviceName() {
		return displayName(device.getDeviceInfo());
	}

	@Override
	public boolean controlsVoice(int voiceIndex) {
		return voiceChannelMap.containsKey(voiceIndex);
	}

	@Override
	public synchronized RealtimeAudioPlayer.MidiSnapshot snapshot(int voiceIndex) {
		if (voiceIndex < 1 || voiceIndex > 3 || !controlsVoice(voiceIndex)) {
			return RealtimeAudioPlayer.MidiSnapshot.off();
		}
		return slots[voiceIndex].snapshot();
	}

	@Override
	public synchronized List<RealtimeAudioPlayer.MidiEvent> drainEvents(int voiceIndex) {
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

	private synchronized void noteOn(int channel, int note, int velocity) {
		if (closed) {
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
		if (closed) {
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
		if (closed) {
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
		if (closed) {
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
		if (closed) {
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

	private final class RoutingReceiver implements Receiver {
		@Override
		public void send(MidiMessage message, long timeStamp) {
			if (closed || !(message instanceof ShortMessage shortMessage)) {
				return;
			}
			int channel = shortMessage.getChannel() + 1;
			switch (shortMessage.getCommand()) {
			case ShortMessage.NOTE_ON -> noteOn(channel, shortMessage.getData1(), shortMessage.getData2());
			case ShortMessage.NOTE_OFF -> noteOff(channel, shortMessage.getData1());
			case ShortMessage.PITCH_BEND -> pitchBend(channel, shortMessage.getData1(), shortMessage.getData2());
			case ShortMessage.CONTROL_CHANGE -> controlChange(channel, shortMessage.getData1(), shortMessage.getData2());
			default -> {
				emit("CMD 0x" + Integer.toHexString(shortMessage.getCommand()) + " ch " + channel + " data "
						+ shortMessage.getData1() + "," + shortMessage.getData2());
			}
			}
		}

		@Override
		public void close() {
			MidiInputRouter.this.close();
		}
	}

	private static final class VoiceSlot {
		int note = -1;
		int velocity = 0;
		boolean gate = false;
		double pitchBendSemitones = 0.0;
		long sequence = 0;
		long noteOnId = 0;
		long noteOffId = 0;
		int lastNote = -1;
		int lastVelocity = 0;
		final Deque<RealtimeAudioPlayer.MidiEvent> events = new ArrayDeque<>();

		void start(int note, int velocity, double pitchBendSemitones, long sequence) {
			this.note = Math.max(0, Math.min(127, note));
			this.velocity = Math.max(1, Math.min(127, velocity));
			this.pitchBendSemitones = pitchBendSemitones;
			this.sequence = sequence;
			this.noteOnId = sequence;
			this.lastNote = this.note;
			this.lastVelocity = this.velocity;
			this.gate = true;
			events.addLast(new RealtimeAudioPlayer.MidiEvent(this.note, this.velocity, true, pitchBendSemitones,
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
			events.addLast(new RealtimeAudioPlayer.MidiEvent(this.lastNote, this.lastVelocity, false,
					pitchBendSemitones, sequence));
			this.velocity = 0;
			this.gate = false;
		}

		RealtimeAudioPlayer.MidiSnapshot snapshot() {
			if (noteOnId == 0 || (note < 0 && lastNote < 0)) {
				return RealtimeAudioPlayer.MidiSnapshot.off();
			}
			int snapshotNote = gate ? note : lastNote;
			int snapshotVelocity = gate ? velocity : lastVelocity;
			return new RealtimeAudioPlayer.MidiSnapshot(snapshotNote, snapshotVelocity, gate, pitchBendSemitones,
					noteOnId, noteOffId);
		}

		List<RealtimeAudioPlayer.MidiEvent> drainEvents() {
			if (events.isEmpty()) {
				return List.of();
			}
			List<RealtimeAudioPlayer.MidiEvent> drained = new ArrayList<>(events);
			events.clear();
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
}
