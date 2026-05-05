/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.midi;

import net.resheim.sidscore.ir.RealtimeAudioPlayer;
import org.junit.Test;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Verifies the Java Sound paths with an in-memory MIDI input instrument.
 * <p>
 * A human reviewer can validate the behavior by reading the mock fixture:
 * the router sees a normal {@link javax.sound.midi.MidiDevice}, installs a
 * receiver on its transmitter, and these tests push MIDI messages through that
 * receiver exactly as a hardware keyboard would.
 */
public class MidiInputRouterMockInstrumentTest {

	@Test
	public void listInputDevicesUsesMockInstrumentProvider() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock Keys");
		MockMidiInstrument outputOnly = new MockMidiInstrument("Output Only", "SIDScore Test",
				"No transmitter", "1.0", 0, false);

		try (AutoCloseable ignored = MockMidiInstrument.install(input, outputOnly)) {
			List<MidiInputRouter.InputDevice> devices = MidiInputRouter.listInputDevices();

			assertEquals("Only mock devices with transmitters are usable MIDI inputs.", 1, devices.size());
			assertEquals("Mock Keys", devices.get(0).displayName());
			assertEquals("SIDScore Test", devices.get(0).vendor());
		}
	}

	@Test
	public void openSelectsMockInstrumentByIndexAndSearchText() throws Exception {
		MockMidiInstrument first = new MockMidiInstrument("First Mock");
		MockMidiInstrument second = new MockMidiInstrument("Second Mock");

		try (AutoCloseable ignored = MockMidiInstrument.install(first, second)) {
			try (MidiInputRouter router = MidiInputRouter.open("1", Map.of(1, 1))) {
				assertEquals("Second Mock", router.deviceName());
				assertTrue(second.isOpen());
				assertTrue(second.hasReceiver());
			}

			try (MidiInputRouter router = MidiInputRouter.open("first", Map.of(1, 1))) {
				assertEquals("First Mock", router.deviceName());
				assertTrue(first.isOpen());
				assertTrue(first.hasReceiver());
			}
		}
	}

	@Test
	public void routesShortMidiMessagesFromMockInstrumentToSidVoice() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock Keys");
		List<String> routedEvents = new ArrayList<>();

		try (AutoCloseable ignored = MockMidiInstrument.install(input);
				MidiInputRouter router = MidiInputRouter.open("", orderedVoiceMap(1, 1, 2, 1), routedEvents::add)) {
			assertTrue("Opening the router must attach a receiver to the mock transmitter before input is sent.",
					input.hasReceiver());
			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 60, 100));
			RealtimeAudioPlayer.MidiSnapshot voice1 = router.snapshot(1);
			assertTrue("NOTE_ON should gate voice 1; transmitterCount=" + input.transmitterCount()
					+ ", hasReceiver=" + input.hasReceiver() + ", events=" + routedEvents, voice1.gate());
			assertEquals(60, voice1.note());
			assertEquals(100, voice1.velocity());
			assertEquals(1, router.drainEvents(1).size());

			input.send(MockMidiInstrument.shortMessage(ShortMessage.PITCH_BEND, 1, 0, 127));
			assertTrue("Pitch bend is carried in semitones for active voices.",
					router.snapshot(1).pitchBendSemitones() > 1.9);

			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_OFF, 1, 60, 0));
			RealtimeAudioPlayer.MidiSnapshot released = router.snapshot(1);
			assertFalse(released.gate());
			assertEquals(60, released.note());

			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 62, 0));
			assertFalse("NOTE_ON with velocity zero follows the MIDI note-off convention.",
					router.snapshot(1).gate());

			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 64, 110));
			input.send(MockMidiInstrument.shortMessage(ShortMessage.CONTROL_CHANGE, 1, 123, 0));
			assertFalse("CC 123 All Notes Off releases mapped voices.", router.snapshot(1).gate());
		}
	}

	@Test
	public void routesRawVoiceMessagesFromMockInstrument() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Raw Mock");

		try (AutoCloseable ignored = MockMidiInstrument.install(input);
				MidiInputRouter router = MidiInputRouter.open("", Map.of(1, 1))) {
			input.send(MockMidiInstrument.raw(0x90, 67, 90));

			RealtimeAudioPlayer.MidiSnapshot snapshot = router.snapshot(1);
			assertTrue(snapshot.gate());
			assertEquals(67, snapshot.note());
			assertEquals(90, snapshot.velocity());
		}
	}

	@Test
	public void suspendResumeAndCloseMirrorRealtimeLifecycle() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Lifecycle Mock");

		try (AutoCloseable ignored = MockMidiInstrument.install(input)) {
			MidiInputRouter router = MidiInputRouter.open("", Map.of(1, 1));

			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 60, 100));
			assertTrue(router.snapshot(1).gate());

			router.suspendInput();
			assertFalse("Suspending input clears held notes before score playback takes over.",
					router.snapshot(1).gate());
			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 62, 100));
			assertFalse("Messages received while suspended are ignored.", router.snapshot(1).gate());

			router.resumeInput();
			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 64, 100));
			assertTrue(router.snapshot(1).gate());
			long noteOnId = router.snapshot(1).noteOnId();

			router.close();
			RealtimeAudioPlayer.MidiSnapshot closed = router.snapshot(1);
			assertFalse("Closing the router emits a final release for held notes.", closed.gate());
			assertTrue(closed.noteOffId() > noteOnId);
			assertEquals(1, input.closeCount());
		}
	}

	@Test
	public void resumeInputDoesNotClearAlreadyLiveMultiVoiceInput() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Live Reuse Mock");
		List<String> routedEvents = new ArrayList<>();

		try (AutoCloseable ignored = MockMidiInstrument.install(input);
				MidiInputRouter router = MidiInputRouter.open("", orderedVoiceMap(1, 1, 2, 1), routedEvents::add)) {
			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 60, 100));
			assertTrue("The first note should be visible before the server reuses the MIDI source.",
					router.snapshot(1).gate());

			router.resumeInput();

			assertTrue("Reusing an already-live source must not erase the note used by the monitor to detect input; "
					+ "events=" + routedEvents, router.snapshot(1).gate());
			assertEquals(60, router.snapshot(1).note());
			assertTrue("The activity id must survive a no-op resume so monitor startup can see it.",
					router.snapshot(1).noteOnId() > 0);
		}
	}

	@Test
	public void remapVoiceChannelsAddsVoiceWithoutReopeningMockInstrument() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Remap Mock");

		try (AutoCloseable ignored = MockMidiInstrument.install(input);
				MidiInputRouter router = MidiInputRouter.open("", Map.of(1, 1))) {
			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 60, 100));
			assertTrue("Voice 1 starts from the original one-voice MIDI map.", router.snapshot(1).gate());

			router.remapVoiceChannels(orderedVoiceMap(1, 1, 2, 1));

			assertEquals("Adding a voice on the same device must keep the Java Sound input open.",
					1, input.openCount());
			assertTrue("Voice 1 keeps its held note across the in-place remap.",
					router.snapshot(1).gate());
			input.send(MockMidiInstrument.shortMessage(ShortMessage.NOTE_ON, 1, 64, 100));
			assertTrue("The next note on the shared MIDI channel is allocated to the newly-added voice.",
					router.snapshot(2).gate());
			assertEquals(64, router.snapshot(2).note());
		}
	}

	@Test
	public void openFailureClosesMockInstrument() throws Exception {
		MockMidiInstrument broken = new MockMidiInstrument("Broken Mock", "SIDScore Test",
				"Transmitter throws", "1.0", 1, true);

		try (AutoCloseable ignored = MockMidiInstrument.install(broken)) {
			MidiUnavailableException error = assertThrows(MidiUnavailableException.class,
					() -> MidiInputRouter.open("", Map.of(1, 1)));

			assertEquals("Mock transmitter open failure", error.getMessage());
			assertEquals(1, broken.openCount());
			assertEquals("The router must close a device if transmitter setup fails.", 1, broken.closeCount());
		}
	}

	/**
	 * Builds a voice map with explicit iteration order. The router allocates the
	 * first free voice from this order when multiple voices share one MIDI channel.
	 */
	private static Map<Integer, Integer> orderedVoiceMap(int... voiceChannelPairs) {
		Map<Integer, Integer> map = new LinkedHashMap<>();
		for (int i = 0; i < voiceChannelPairs.length; i += 2) {
			map.put(voiceChannelPairs[i], voiceChannelPairs[i + 1]);
		}
		return map;
	}
}
