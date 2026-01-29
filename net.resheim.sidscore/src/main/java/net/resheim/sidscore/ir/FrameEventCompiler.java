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
package net.resheim.sidscore.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiles TimedScore voice events into the same frame-based event stream
 * used by the exported ASM player. This keeps realtime playback and exports
 * aligned.
 */
public final class FrameEventCompiler {
	public static final record FrameEvent(int frames, int freq, int ctrl, int baseNote) {
	}

	private static final double SID_CLOCK_NTSC = 1022727.0;
	private static final double SID_CLOCK_PAL = 985248.0;
	private static final double NOISE_HZ = 1000.0;
	private static final double RASTER_RATE_PAL = 50.124542;
	private static final double RASTER_RATE_NTSC = 60.098814;
	private static final int RETRIG_GAP_FRAMES = 1;

	private FrameEventCompiler() {
	}

	public static List<FrameEvent> compileVoice(SIDScoreIR.TimedVoice voice, SIDScoreIR.TimedScore score) {
		List<FrameEvent> out = new ArrayList<>();
		if (voice == null || voice.instrument() == null) {
			out.add(new FrameEvent(0, 0, 0, 0));
			return out;
		}

		SIDScoreIR.InstrumentIR instr = voice.instrument();

		double ticksPerQuarter = score.ticksPerWhole() / 4.0;
		double secondsPerTick = 60.0 / score.tempoBpm() / ticksPerQuarter;
		double frameRate = score.system() == SIDScoreIR.VideoSystem.PAL ? RASTER_RATE_PAL : RASTER_RATE_NTSC;
		double rem = 0.0;

		int waveBits = waveMaskToCtrl(instr.waveMask());
		int ctrlBase = waveBits | (instr.sync() ? 0x02 : 0x00) | (instr.ring() ? 0x04 : 0x00);
		boolean lastGateOn = false;

		for (SIDScoreIR.TimedEvent ev : voice.events()) {
			double framesExact = ev.durationTicks() * secondsPerTick * frameRate + rem;
			int frames = (int) Math.max(1, Math.round(framesExact));
			rem = framesExact - frames;

			if ((ev.type() == SIDScoreIR.TimedType.NOTE || ev.type() == SIDScoreIR.TimedType.NOISE)
					&& instr.gateMin() > 0) {
				frames = Math.max(frames, instr.gateMin());
			}

			int freq = 0;
			int ctrl = ctrlBase;
			int baseNote = 0;
			if (ev.type() == SIDScoreIR.TimedType.NOTE) {
				SIDScoreIR.GateMode gate = ev.gateMode().orElse(SIDScoreIR.GateMode.RETRIG);
				int midi = ev.pitchMidi().orElseThrow();
				freq = freqRegFromMidi(midi, score.system());
				baseNote = clampMidi(midi);
				if (gate == SIDScoreIR.GateMode.RETRIG && lastGateOn && frames > RETRIG_GAP_FRAMES) {
					out.add(new FrameEvent(RETRIG_GAP_FRAMES, freq, ctrlBase, baseNote));
					frames -= RETRIG_GAP_FRAMES;
				}
				ctrl |= 0x01;
				lastGateOn = true;
			} else if (ev.type() == SIDScoreIR.TimedType.NOISE) {
				SIDScoreIR.GateMode gate = ev.gateMode().orElse(SIDScoreIR.GateMode.RETRIG);
				freq = freqRegFromHz(NOISE_HZ, score.system());
				baseNote = 0x80;
				if (gate == SIDScoreIR.GateMode.RETRIG && lastGateOn && frames > RETRIG_GAP_FRAMES) {
					out.add(new FrameEvent(RETRIG_GAP_FRAMES, freq, ctrlBase, baseNote));
					frames -= RETRIG_GAP_FRAMES;
				}
				ctrl = 0x80 | (instr.sync() ? 0x02 : 0x00) | (instr.ring() ? 0x04 : 0x00) | 0x01;
				lastGateOn = true;
			} else {
				lastGateOn = false;
			}

			while (frames > 0) {
				int chunk = Math.min(frames, 0xFFFF);
				out.add(new FrameEvent(chunk, freq, ctrl, baseNote));
				frames -= chunk;
			}
		}

		out.add(new FrameEvent(0, 0, 0, 0));
		return out;
	}

	public static List<Integer> toBytes(List<FrameEvent> events) {
		List<Integer> out = new ArrayList<>();
		for (FrameEvent ev : events) {
			out.add(ev.frames() & 0xff);
			out.add((ev.frames() >> 8) & 0xff);
			out.add(ev.freq() & 0xff);
			out.add((ev.freq() >> 8) & 0xff);
			out.add(ev.ctrl() & 0xff);
			out.add(ev.baseNote() & 0xff);
		}
		return out;
	}

	private static int clampMidi(int midi) {
		if (midi < 0)
			return 0;
		if (midi > 127)
			return 127;
		return midi;
	}

	private static int freqRegFromMidi(int midi, SIDScoreIR.VideoSystem system) {
		double hz = 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
		return freqRegFromHz(hz, system);
	}

	private static int freqRegFromHz(double hz, SIDScoreIR.VideoSystem system) {
		double fclk = system == SIDScoreIR.VideoSystem.PAL ? SID_CLOCK_PAL : SID_CLOCK_NTSC;
		int reg = (int) Math.round(hz * 16777216.0 / fclk);
		if (reg < 1)
			reg = 1;
		if (reg > 0xFFFF)
			reg = 0xFFFF;
		return reg;
	}

	private static int waveMaskToCtrl(int waveMask) {
		int ctrl = 0;
		if ((waveMask & SIDScoreIR.Wave.TRI.mask) != 0)
			ctrl |= 0x10;
		if ((waveMask & SIDScoreIR.Wave.SAW.mask) != 0)
			ctrl |= 0x20;
		if ((waveMask & SIDScoreIR.Wave.PULSE.mask) != 0)
			ctrl |= 0x40;
		if ((waveMask & SIDScoreIR.Wave.NOISE.mask) != 0)
			ctrl |= 0x80;
		return ctrl;
	}

}
