/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.export;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.resheim.sidscore.ir.SIDScoreIR;

/** Coordinates music state and effect ownership across every tune in a module. */
final class SIDScoreModuleAsmGenerator {
	private SIDScoreModuleAsmGenerator() {}

	static String generate(List<SIDScoreIR.TimedScore> tunes, String namespace,
			Function<SIDScoreIR.TimedScore, String> musicGenerator) {
		if (tunes == null || tunes.isEmpty()) throw new IllegalArgumentException("At least one tune is required");
		if (namespace == null || !namespace.matches("[A-Za-z_][A-Za-z0-9_]*")) {
			throw new IllegalArgumentException("Invalid KickAssembler namespace: " + namespace);
		}
		if (tunes.size() > 254) throw new IllegalArgumentException("Embedded module supports at most 254 tunes");
		List<SIDScoreIR.EffectIR> effects = new ArrayList<>();
		List<SIDScoreIR.VideoSystem> systems = new ArrayList<>();
		StringBuilder out = new StringBuilder(".namespace ").append(namespace).append(" {\n");
		out.append(".const TMP_PTR = $fb\n");
		out.append("// A=1..").append(tunes.size()).append(" selects tune; invalid A selects tune 1.\n");
		out.append("// Shared effect ownership, zero-page $fb-$fc; host owns IRQ and placement.\n");
		out.append("init:\n  cmp #1\n  bcc sidscore_select_first\n  cmp #")
				.append(tunes.size() + 1).append("\n  bcc sidscore_select_valid\n");
		out.append("sidscore_select_first:\n  lda #1\nsidscore_select_valid:\n  sta sidscore_selected_tune\n");
		out.append("  jsr effects_clear\n  lda #0\n  ldx #24\nsidscore_clear_hardware:\n")
				.append("  sta $d400,x\n  dex\n  bpl sidscore_clear_hardware\n");
		for (int i = 1; i <= tunes.size(); i++) {
			out.append("  lda sidscore_selected_tune\n  cmp #").append(i)
					.append("\n  bne sidscore_init_next_").append(i).append("\n")
					.append("  jsr tune").append(i).append(".init\n")
					.append("  jsr sidscore_restore_globals\n  jsr sidscore_publish_globals\n")
					.append("  jsr sidscore_start_").append(i).append("\n")
					.append("  rts\nsidscore_init_next_").append(i).append(":\n");
		}
		out.append("  rts\n\nplay:\n");
		for (int i = 1; i <= tunes.size(); i++) {
			out.append("  lda sidscore_selected_tune\n  cmp #").append(i)
					.append("\n  bne sidscore_play_next_").append(i).append("\n")
					.append("  jsr tune").append(i).append(".play\n")
					.append("sidscore_play_next_").append(i).append(":\n");
		}
		out.append("  jsr sidscore_restore_globals\n  jsr effects_play\n  jmp sidscore_publish_globals\n\n");
		for (int i = 1; i <= tunes.size(); i++) {
			out.append("sidscore_start_").append(i).append(":\n");
			for (SIDScoreIR.EffectIR effect : tunes.get(i - 1).effects().values()) {
				out.append("  jsr tune").append(i).append(".effect_").append(effect.name()).append("\n");
			}
			out.append("  rts\n");
		}
		out.append("sidscore_selected_tune:\n  .byte 1\nsidscore_music_sid:\n  .fill 25,0\nsidscore_effect_globals:\n  .fill 4,0\n");
		appendMusicWriters(out);
		appendRestore(out, tunes.stream().anyMatch(tune -> !tune.effects().isEmpty()));
		for (SIDScoreIR.EffectIR effect : tunes.get(0).effects().values()) {
			out.append("effect_").append(effect.name()).append(":\n  jmp tune1.effect_")
					.append(effect.name()).append("\n");
		}
		for (int i = 1; i <= tunes.size(); i++) {
			SIDScoreIR.TimedScore score = tunes.get(i - 1);
			out.append(".namespace tune").append(i).append(" {\n");
			// Each intercepted STA remains three bytes, preserving existing branch distances.
			String music = musicGenerator.apply(score);
			for (int register = 0; register < 21; register++) {
				music = music.replace("  sta $" + String.format("%04X", 0xd400 + register) + "\n",
						"  jsr sidscore_write_" + register + "\n");
			}
			out.append(music);
			for (SIDScoreIR.EffectIR effect : score.effects().values()) {
				String name = "tune" + i + "_" + effect.name();
				out.append("effect_").append(effect.name()).append(":\n  jmp effect_").append(name).append("\n");
				effects.add(new SIDScoreIR.EffectIR(name, effect.preferredVoice(), effect.lengthTicks(),
						effect.priority(), effect.retriggerMode(), effect.steps()));
				systems.add(score.system());
			}
			out.append("}\n");
		}
		SIDScoreEffectAsmGenerator.append(out, effects, systems);
		return out.append("}\n").toString();
	}

	private static void appendMusicWriters(StringBuilder out) {
		for (int register = 0; register < 21; register++) {
			out.append("sidscore_write_").append(register).append(":\n  sta sidscore_music_sid+")
					.append(register).append("\n  php\n  pha\n  lda sfx_owner+").append(register / 7 + 1)
					.append("\n  bne sidscore_write_owned_").append(register).append("\n")
					.append("  pla\n  sta $").append(String.format("%04x", 0xd400 + register))
					.append("\n  plp\n  rts\nsidscore_write_owned_").append(register)
					.append(":\n  pla\n  plp\n  rts\n");
		}
	}

	private static void appendRestore(StringBuilder out, boolean hasEffects) {
		out.append("sidscore_restore_globals:\n");
		for (int register = 21; register <= 24; register++) {
			out.append("  lda sidscore_music_sid+").append(register).append("\n  sta sidscore_effect_globals+")
					.append(register - 21).append("\n");
			if (register == 23) out.append("  sta sfx_shadow417\n");
			if (register == 24) out.append("  sta sfx_shadow418\n");
		}
		out.append("  rts\nsidscore_publish_globals:\n");
		for (int register = 21; register <= 24; register++) {
			out.append("  lda sidscore_effect_globals+").append(register - 21).append("\n  sta $")
					.append(String.format("%04x", 0xd400 + register)).append("\n");
		}
		out.append("  rts\n");
		if (!hasEffects) return;
		out.append("sidscore_restore_voice:\n  ldx sfx_work_voice\n")
				.append("  lda sfx_sid_ctrl_offset,x\n  tax\n  lda #0\n  sta $d400,x\n")
				.append("  ldx sfx_work_voice\n  lda sfx_sid_offset,x\n  tax\n");
		// Restore envelope/frequency/pulse first, then re-enable the music gate.
		for (int register : new int[] { 0, 1, 2, 3, 5, 6, 4 }) {
			out.append("  lda sidscore_music_sid+").append(register).append(",x\n  sta $")
					.append(String.format("%04x", 0xd400 + register)).append(",x\n");
		}
		out.append("  rts\n");
	}
}
