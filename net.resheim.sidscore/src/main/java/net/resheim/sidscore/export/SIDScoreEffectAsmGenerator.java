/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.export;

import java.util.ArrayList;
import java.util.List;

import net.resheim.sidscore.ir.SIDScoreIR;

/** Emits on-demand frame-based effects for a relocatable module. */
final class SIDScoreEffectAsmGenerator {
	private static final double PAL_CLOCK = 985248.0;
	private static final double NTSC_CLOCK = 1022727.0;
	private static final int FRAME_BYTES = 14;

	private SIDScoreEffectAsmGenerator() {}

	static void append(StringBuilder out, SIDScoreIR.TimedScore score) {
		List<SIDScoreIR.EffectIR> effects = new ArrayList<>(score.effects().values());
		if (effects.size() > 255) throw new IllegalArgumentException("Module supports at most 255 effects per tune");
		out.append("\n// On-demand SIDScore effects. Call effect_<name>, then play each video frame.\n");
		out.append("effects_clear:\n");
		if (effects.isEmpty()) {
			out.append("  lda #0\n  sta sfx_shadow417\n  lda #$0f\n  sta sfx_shadow418\n  rts\n");
			out.append("effects_start:\n  rts\n\neffects_play:\n  rts\n");
			out.append("sfx_shadow417:\n  .byte 0\nsfx_shadow418:\n  .byte $0f\n");
			return;
		}
		out.append("  lda #0\n  sta sfx_shadow417\n  lda #$0f\n  sta sfx_shadow418\n");
		out.append("  lda #0\n  ldx #").append(effects.size()).append("\n");
		out.append("sfx_clear_effect_loop:\n  sta sfx_voice,x\n  sta sfx_ptr_lo,x\n  sta sfx_ptr_hi,x\n");
		out.append("  dex\n  bne sfx_clear_effect_loop\n");
		out.append("  sta sfx_voice\n  sta sfx_ptr_lo\n  sta sfx_ptr_hi\n");
		out.append("  ldx #3\nsfx_clear_owner_loop:\n  sta sfx_owner,x\n  dex\n  bpl sfx_clear_owner_loop\n  rts\n\n");
		out.append("effects_start:\n");
		for (SIDScoreIR.EffectIR effect : effects) out.append("  jsr effect_").append(effect.name()).append("\n");
		out.append("  rts\n\n");
		out.append("effects_play:\n  lda TMP_PTR\n  sta sfx_saved_ptr\n  lda TMP_PTR+1\n  sta sfx_saved_ptr+1\n");
		for (int id = 1; id <= effects.size(); id++) out.append("  jsr sfx_play_").append(id).append("\n");
		out.append("  lda sfx_saved_ptr\n  sta TMP_PTR\n  lda sfx_saved_ptr+1\n  sta TMP_PTR+1\n  rts\n\n");

		for (int id = 1; id <= effects.size(); id++) {
			SIDScoreIR.EffectIR effect = effects.get(id - 1);
			if (!effect.name().matches("[A-Za-z_][A-Za-z0-9_]*")) {
				throw new IllegalArgumentException("Invalid effect name: " + effect.name());
			}
			out.append("effect_").append(effect.name()).append(":\n");
			out.append("  lda sfx_voice+").append(id).append("\n  beq sfx_new_").append(id).append("\n");
			if (effect.retriggerMode() != SIDScoreIR.EffectRetriggerMode.IGNORE) {
				out.append("  lda #<sfx_data_").append(id).append("\n  sta sfx_ptr_lo+").append(id).append("\n");
				out.append("  lda #>sfx_data_").append(id).append("\n  sta sfx_ptr_hi+").append(id).append("\n");
			}
			out.append("  rts\nsfx_new_").append(id).append(":\n");
			out.append("  lda #").append(id).append("\n  sta sfx_request_id\n");
			out.append("  lda #").append(effect.preferredVoice().orElse(0)).append("\n  sta sfx_request_voice\n");
			out.append("  lda #").append(effect.priority()).append("\n  sta sfx_request_priority\n");
			out.append("  lda #").append(effect.retriggerMode() == SIDScoreIR.EffectRetriggerMode.STEAL ? 1 : 0)
					.append("\n  sta sfx_request_steal\n  jmp sfx_trigger\n\n");
			out.append("sfx_play_").append(id).append(":\n  lda sfx_voice+").append(id)
					.append("\n  beq sfx_play_done_").append(id).append("\n  sta sfx_work_voice\n");
			out.append("  lda sfx_ptr_hi+").append(id).append("\n  cmp #>sfx_end_").append(id)
					.append("\n  bne sfx_play_apply_").append(id).append("\n");
			out.append("  lda sfx_ptr_lo+").append(id).append("\n  cmp #<sfx_end_").append(id)
					.append("\n  bne sfx_play_apply_").append(id).append("\n");
			out.append("  ldx sfx_work_voice\n  lda #0\n  sta sfx_owner,x\n  sta sfx_voice+").append(id).append("\n");
			if (controlsVoice(effect)) {
				out.append("  lda sfx_sid_ctrl_offset,x\n  tax\n  lda #0\n  sta $d400,x\n");
			}
			out.append("  jmp sfx_play_done_").append(id).append("\n");
			out.append("sfx_play_apply_").append(id).append(":\n");
			out.append("  lda sfx_ptr_lo+").append(id).append("\n  sta TMP_PTR\n");
			out.append("  lda sfx_ptr_hi+").append(id).append("\n  sta TMP_PTR+1\n  jsr sfx_apply\n");
			out.append("  clc\n  lda sfx_ptr_lo+").append(id).append("\n  adc #").append(FRAME_BYTES)
					.append("\n  sta sfx_ptr_lo+").append(id).append("\n  lda sfx_ptr_hi+").append(id)
					.append("\n  adc #0\n  sta sfx_ptr_hi+").append(id).append("\n");
			out.append("sfx_play_done_").append(id).append(":\n  rts\n\n");
		}

		appendTriggerRuntime(out);
		appendApplyRuntime(out);
		appendState(out, effects);
		for (int id = 1; id <= effects.size(); id++) {
			SIDScoreIR.EffectIR effect = effects.get(id - 1);
			List<Frame> frames = compile(effect, score.system());
			out.append("sfx_data_").append(id).append(":\n");
			for (Frame frame : frames) {
				out.append("  .byte ").append(byteHex(frame.freq)).append(',').append(byteHex(frame.freq >> 8))
						.append(',').append(byteHex(frame.pw)).append(',').append(byteHex(frame.pw >> 8))
						.append(',').append(byteHex(frame.ctrl())).append(',').append(byteHex(frame.ad()))
						.append(',').append(byteHex(frame.sr())).append(',').append(byteHex(frame.cutoff & 7))
						.append(',').append(byteHex(frame.cutoff >> 3)).append(',').append(byteHex(frame.mask417))
						.append(',').append(byteHex(frame.value417)).append(',').append(byteHex(frame.mask418))
						.append(',').append(byteHex(frame.value418)).append(',')
						.append(byteHex((frame.voiceTouched ? 1 : 0) | (frame.cutoffTouched ? 2 : 0)))
						.append("\n");
			}
			out.append("sfx_end_").append(id).append(":\n");
		}
	}

	private static void appendTriggerRuntime(StringBuilder out) {
		out.append("sfx_trigger:\n  lda sfx_request_voice\n  beq sfx_find_any\n  tax\n  jmp sfx_check_voice\n");
		out.append("sfx_find_any:\n  ldx #3\nsfx_find_free:\n  lda sfx_owner,x\n  beq sfx_assign\n");
		out.append("  dex\n  bne sfx_find_free\n  lda sfx_request_steal\n  beq sfx_reject\n");
		out.append("  ldx #3\nsfx_find_victim:\n  stx sfx_candidate\n  lda sfx_owner,x\n  tax\n");
		out.append("  lda sfx_priority,x\n  cmp sfx_request_priority\n  bcc sfx_steal_candidate\n");
		out.append("  ldx sfx_candidate\n  dex\n  bne sfx_find_victim\n  rts\n");
		out.append("sfx_steal_candidate:\n  ldx sfx_candidate\n  jmp sfx_assign\n");
		out.append("sfx_check_voice:\n  lda sfx_owner,x\n  beq sfx_assign\n");
		out.append("  lda sfx_request_steal\n  beq sfx_reject\n  stx sfx_candidate\n");
		out.append("  lda sfx_owner,x\n  tax\n  lda sfx_priority,x\n  cmp sfx_request_priority\n");
		out.append("  bcs sfx_reject\n  ldx sfx_candidate\n");
		out.append("sfx_assign:\n  lda sfx_owner,x\n  beq sfx_assign_free\n");
		out.append("  tay\n  lda #0\n  sta sfx_voice,y\nsfx_assign_free:\n");
		out.append("  lda sfx_request_id\n  sta sfx_owner,x\n  tay\n  txa\n  sta sfx_voice,y\n");
		out.append("  lda sfx_start_lo,y\n  sta sfx_ptr_lo,y\n  lda sfx_start_hi,y\n  sta sfx_ptr_hi,y\n");
		out.append("sfx_reject:\n  rts\n\n");
	}

	private static void appendApplyRuntime(StringBuilder out) {
		out.append("sfx_apply:\n  ldy #13\n  lda (TMP_PTR),y\n  and #1\n  beq sfx_no_voice\n");
		out.append("  ldx sfx_work_voice\n  lda sfx_sid_offset,x\n  tax\n");
		for (int i = 0; i < 7; i++) {
			out.append("  ldy #").append(i).append("\n  lda (TMP_PTR),y\n  sta $d400,x\n");
			if (i < 6) out.append("  inx\n");
		}
		out.append("sfx_no_voice:\n  ldy #13\n  lda (TMP_PTR),y\n  and #2\n  beq sfx_no_cutoff\n");
		out.append("  ldy #7\n  lda (TMP_PTR),y\n  sta $d415\n  iny\n  lda (TMP_PTR),y\n  sta $d416\n");
		out.append("sfx_no_cutoff:\n  ldy #9\n  lda (TMP_PTR),y\n  beq sfx_no_resfilt\n");
		out.append("  eor #$ff\n  and sfx_shadow417\n  sta sfx_tmp\n  iny\n  lda (TMP_PTR),y\n  ora sfx_tmp\n  sta sfx_shadow417\n  sta $d417\n");
		out.append("sfx_no_resfilt:\n  ldy #11\n  lda (TMP_PTR),y\n  beq sfx_no_modevol\n");
		out.append("  eor #$ff\n  and sfx_shadow418\n  sta sfx_tmp\n  iny\n  lda (TMP_PTR),y\n  ora sfx_tmp\n  sta sfx_shadow418\n  sta $d418\n");
		out.append("sfx_no_modevol:\n  rts\n\n");
	}

	private static void appendState(StringBuilder out, List<SIDScoreIR.EffectIR> effects) {
		out.append("sfx_owner:\n  .byte 0,0,0,0\n");
		out.append("sfx_voice:\n  .byte 0");
		for (int ignored = 0; ignored < effects.size(); ignored++) out.append(",0");
		out.append("\nsfx_ptr_lo:\n  .byte 0");
		for (int ignored = 0; ignored < effects.size(); ignored++) out.append(",0");
		out.append("\nsfx_ptr_hi:\n  .byte 0");
		for (int ignored = 0; ignored < effects.size(); ignored++) out.append(",0");
		out.append("\nsfx_priority:\n  .byte 0");
		for (SIDScoreIR.EffectIR effect : effects) out.append(',').append(effect.priority());
		out.append("\nsfx_start_lo:\n  .byte 0");
		for (int id = 1; id <= effects.size(); id++) out.append(",<sfx_data_").append(id);
		out.append("\nsfx_start_hi:\n  .byte 0");
		for (int id = 1; id <= effects.size(); id++) out.append(",>sfx_data_").append(id);
		out.append("\nsfx_sid_offset:\n  .byte 0,0,7,14\n");
		out.append("sfx_sid_ctrl_offset:\n  .byte 0,4,11,18\n");
		out.append("sfx_shadow417:\n  .byte 0\nsfx_shadow418:\n  .byte $0f\n");
		for (String name : List.of("sfx_saved_ptr", "sfx_request_id", "sfx_request_voice", "sfx_request_priority",
				"sfx_request_steal", "sfx_candidate", "sfx_work_voice", "sfx_tmp")) {
			out.append(name).append(":\n  ").append(name.equals("sfx_saved_ptr") ? ".word 0" : ".byte 0")
					.append("\n");
		}
		out.append("\n");
	}

	private static boolean controlsVoice(SIDScoreIR.EffectIR effect) {
		for (SIDScoreIR.EffectStepIR step : effect.steps()) {
			SIDScoreIR.EffectParameter p = step instanceof SIDScoreIR.EffectAssignmentIR a ? a.parameter()
					: ((SIDScoreIR.EffectSweepIR) step).parameter();
			if (p != SIDScoreIR.EffectParameter.FILTER && p != SIDScoreIR.EffectParameter.FILTERROUTE
					&& p != SIDScoreIR.EffectParameter.CUTOFF && p != SIDScoreIR.EffectParameter.RES
					&& p != SIDScoreIR.EffectParameter.VOLUME) return true;
		}
		return false;
	}

	private static List<Frame> compile(SIDScoreIR.EffectIR effect, SIDScoreIR.VideoSystem system) {
		if (effect.lengthTicks() < 1 || effect.lengthTicks() > 4096) {
			throw new IllegalArgumentException("EFFECT " + effect.name() + " length must be 1..4096 frames for module export");
		}
		List<Frame> frames = new ArrayList<>(effect.lengthTicks());
		for (int i = 0; i < effect.lengthTicks(); i++) frames.add(new Frame());
		for (SIDScoreIR.EffectStepIR step : effect.steps()) {
			if (step instanceof SIDScoreIR.EffectAssignmentIR assignment) {
				int tick = assignment.tick();
				if (assignment.parameter() == SIDScoreIR.EffectParameter.RESET) {
					if (tick < frames.size()) frames.get(tick).reset = true;
					for (int i = tick; i < frames.size(); i++) frames.get(i).voiceTouched = true;
				} else {
					for (int i = tick; i < frames.size(); i++) apply(frames.get(i), assignment.parameter(),
							assignment.value(), system);
				}
			} else if (step instanceof SIDScoreIR.EffectSweepIR sweep) {
				int duration = Math.max(1, Math.min(sweep.durationTicks(), frames.size()));
				for (int i = 0; i < duration; i++) {
					double progress = duration == 1 ? 1.0 : i / (double) (duration - 1);
					progress = switch (sweep.curve()) {
						case EXP -> progress * progress;
						case LOG -> Math.sqrt(progress);
						case LINEAR, STEP -> progress;
					};
					int from = sweep.fromValue().value();
					int to = sweep.toValue().value();
					int value = (int) Math.round(from + (to - from) * progress);
					apply(frames.get(i), sweep.parameter(), SIDScoreIR.EffectValueIR.integer(value), system);
				}
			} else {
				throw new IllegalArgumentException("Unsupported effect step in " + effect.name());
			}
		}
		return frames;
	}

	private static void apply(Frame f, SIDScoreIR.EffectParameter p, SIDScoreIR.EffectValueIR value,
			SIDScoreIR.VideoSystem system) {
		int v = value.value();
		switch (p) {
			case WAVE -> { f.wave = waveBits(v); f.voiceTouched = true; }
			case GATE -> { f.gate = v != 0; f.voiceTouched = true; }
			case SYNC -> { f.sync = v != 0; f.voiceTouched = true; }
			case RING -> { f.ring = v != 0; f.voiceTouched = true; }
			case PITCH -> { f.freq = frequency(v, system); f.voiceTouched = true; }
			case FREQ -> { f.freq = clamp(v, 0xffff); f.voiceTouched = true; }
			case PW -> { f.pw = clamp(v, 0x0fff); f.voiceTouched = true; }
			case HIPULSE -> { f.pw = (f.pw & 0xff) | ((v & 15) << 8); f.voiceTouched = true; }
			case LOWPULSE -> { f.pw = (f.pw & 0xf00) | (v & 255); f.voiceTouched = true; }
			case ADSR -> {
				SIDScoreIR.AdsrIR adsr = value.adsr().orElseThrow();
				f.attack = adsr.a(); f.decay = adsr.d(); f.sustain = adsr.s(); f.release = adsr.r();
				f.voiceTouched = true;
			}
			case ATTACK -> { f.attack = clamp(v, 15); f.voiceTouched = true; }
			case DECAY -> { f.decay = clamp(v, 15); f.voiceTouched = true; }
			case SUSTAIN -> { f.sustain = clamp(v, 15); f.voiceTouched = true; }
			case RELEASE -> { f.release = clamp(v, 15); f.voiceTouched = true; }
			case FILTER -> { f.mask418 |= 0x70; f.value418 = (f.value418 & ~0x70) | filterBits(v); }
			case FILTERROUTE -> { f.mask417 |= 0x0f; f.value417 = (f.value417 & ~0x0f) | (v & 15); }
			case CUTOFF -> { f.cutoff = clamp(v, 0x07ff); f.cutoffTouched = true; }
			case RES -> { f.mask417 |= 0xf0; f.value417 = (f.value417 & ~0xf0) | ((v & 15) << 4); }
			case VOLUME -> { f.mask418 |= 0x0f; f.value418 = (f.value418 & ~0x0f) | (v & 15); }
			case RESET -> throw new IllegalArgumentException("RESET must be handled as an instantaneous assignment");
			default -> throw new IllegalArgumentException("Unsupported effect parameter " + p);
		}
	}

	private static int frequency(int midi, SIDScoreIR.VideoSystem system) {
		double hz = 440.0 * Math.pow(2.0, (clamp(midi, 127) - 69) / 12.0);
		double clock = system == SIDScoreIR.VideoSystem.PAL ? PAL_CLOCK : NTSC_CLOCK;
		return Math.max(1, Math.min(0xffff, (int) Math.round(hz * 16777216.0 / clock)));
	}

	private static int waveBits(int mask) {
		int bits = 0;
		if ((mask & SIDScoreIR.Wave.TRI.mask) != 0) bits |= 0x10;
		if ((mask & SIDScoreIR.Wave.SAW.mask) != 0) bits |= 0x20;
		if ((mask & SIDScoreIR.Wave.PULSE.mask) != 0) bits |= 0x40;
		if ((mask & SIDScoreIR.Wave.NOISE.mask) != 0) bits |= 0x80;
		return bits;
	}

	private static int filterBits(int mask) {
		int bits = 0;
		if ((mask & SIDScoreIR.FilterMode.LP.mask) != 0) bits |= 0x10;
		if ((mask & SIDScoreIR.FilterMode.BP.mask) != 0) bits |= 0x20;
		if ((mask & SIDScoreIR.FilterMode.HP.mask) != 0) bits |= 0x40;
		return bits;
	}

	private static int clamp(int value, int max) { return Math.max(0, Math.min(max, value)); }
	private static String byteHex(int value) { return "$" + String.format("%02x", value & 255); }

	private static final class Frame {
		int freq;
		int pw = 0x0800;
		int wave;
		boolean gate, sync, ring, reset, voiceTouched, cutoffTouched;
		int attack, decay, sustain, release, cutoff, mask417, value417, mask418, value418;
		int ctrl() { return wave | (gate ? 1 : 0) | (sync ? 2 : 0) | (ring ? 4 : 0) | (reset ? 8 : 0); }
		int ad() { return (attack << 4) | decay; }
		int sr() { return (sustain << 4) | release; }
	}
}
