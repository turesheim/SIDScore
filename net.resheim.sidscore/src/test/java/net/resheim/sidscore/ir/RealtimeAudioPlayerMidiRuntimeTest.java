/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.ir;

import net.resheim.sidscore.sid.SidModel;
import net.resheim.sidscore.sid.SidWaveforms;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

public class RealtimeAudioPlayerMidiRuntimeTest {

	private static final float SAMPLE_RATE = 44_100f;
	private static final double SID_CLOCK_PAL = 985_248.0;
	private static final double RASTER_RATE_PAL = 50.124542;

	@Test
	public void repeatedMidiNoteStartsWithStableOscillatorPhaseAfterSilence() {
		ScriptedMidiSource midi = new ScriptedMidiSource();
		RealtimeAudioPlayer.MidiRuntime runtime = newMidiRuntime(midi);

		midi.noteOn(60, 100);
		double[] firstAttack = renderSamples(runtime, 512);

		midi.noteOff(60);
		renderSamples(runtime, 5_000);
		assertFalse("The release should reach silence before the next attack.", runtime.ownsVoice());

		midi.noteOn(60, 100);
		double[] secondAttack = renderSamples(runtime, 512);

		assertArrayEquals("Repeating the same MIDI key after silence should start with the same waveform phase.",
				firstAttack, secondAttack, 1.0e-12);
	}

	private static RealtimeAudioPlayer.MidiRuntime newMidiRuntime(ScriptedMidiSource midi) {
		SIDScoreIR.InstrumentIR instrument = new SIDScoreIR.InstrumentIR("midi-test",
				SIDScoreIR.Wave.PULSE.mask, new SIDScoreIR.AdsrIR(0, 0, 15, 0), OptionalInt.of(0x0800),
				OptionalInt.empty(), OptionalInt.empty(), 0, Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), 0, OptionalInt.empty(), OptionalInt.empty(), Optional.empty(),
				SIDScoreIR.InstrumentGateMode.RETRIGGER, 0, false, false);
		SidWaveforms.TableSet waveTables = SidWaveforms.loadTables(SidModel.MOS6581, null);
		return new RealtimeAudioPlayer.MidiRuntime(1, instrument, midi, SID_CLOCK_PAL, RASTER_RATE_PAL,
				Map.of(), new RealtimeAudioPlayer.FilterRuntime(RASTER_RATE_PAL), waveTables, null);
	}

	private static double[] renderSamples(RealtimeAudioPlayer.MidiRuntime runtime, int count) {
		double[] out = new double[count];
		for (int i = 0; i < count; i++) {
			runtime.prepareSample(SAMPLE_RATE);
			runtime.advanceOsc(SAMPLE_RATE);
			runtime.applySync(false);
			out[i] = runtime.renderSample(SAMPLE_RATE, false);
		}
		return out;
	}

	private static final class ScriptedMidiSource implements RealtimeAudioPlayer.MidiSource {
		private final Queue<RealtimeAudioPlayer.MidiEvent> events = new ArrayDeque<>();
		private long nextId = 1;
		private int note = -1;
		private int velocity = 0;
		private boolean gate = false;
		private long noteOnId = 0;
		private long noteOffId = 0;

		void noteOn(int note, int velocity) {
			long id = nextId++;
			this.note = note;
			this.velocity = velocity;
			this.gate = true;
			this.noteOnId = id;
			events.offer(new RealtimeAudioPlayer.MidiEvent(note, velocity, true, 0.0, id));
		}

		void noteOff(int note) {
			long id = nextId++;
			this.gate = false;
			this.noteOffId = id;
			events.offer(new RealtimeAudioPlayer.MidiEvent(note, velocity, false, 0.0, id));
			this.velocity = 0;
		}

		@Override
		public boolean controlsVoice(int voiceIndex) {
			return voiceIndex == 1;
		}

		@Override
		public RealtimeAudioPlayer.MidiSnapshot snapshot(int voiceIndex) {
			if (noteOnId == 0) {
				return RealtimeAudioPlayer.MidiSnapshot.off();
			}
			return new RealtimeAudioPlayer.MidiSnapshot(note, velocity, gate, 0.0, noteOnId, noteOffId);
		}

		@Override
		public List<RealtimeAudioPlayer.MidiEvent> drainEvents(int voiceIndex) {
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
}
