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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public final class SIDScoreIR {

	// -------------------------
	// Diagnostics (for warnings + internal errors during resolve)
	// -------------------------
	public static final class Diagnostics {
		public enum Severity {
			WARNING, ERROR
		}

		public static final record Message(Severity severity, String text) {
		}

		private final List<Message> messages = new ArrayList<>();

		public void warn(String s) {
			messages.add(new Message(Severity.WARNING, s));
		}

		public void error(String s) {
			messages.add(new Message(Severity.ERROR, s));
		}

		public boolean hasErrors() {
			return messages.stream().anyMatch(m -> m.severity == Severity.ERROR);
		}

		public List<Message> messages() {
			return List.copyOf(messages);
		}

		public void throwIfErrors() {
			if (!hasErrors())
				return;
			StringBuilder sb = new StringBuilder("Resolve failed:\n");
			for (Message m : messages) {
				if (m.severity == Severity.ERROR)
					sb.append("  ERROR: ").append(m.text).append('\n');
			}
			throw new IllegalStateException(sb.toString());
		}
	}

	// -------------------------
	// IR Level 1: ScoreIR
	// -------------------------
	public enum Wave {
		PULSE(1 << 0), SAW(1 << 1), TRI(1 << 2), NOISE(1 << 3);

		public final int mask;

		Wave(int mask) {
			this.mask = mask;
		}
	}

	public static final record AdsrIR(int a, int d, int s, int r) {
	}

	public enum TableType {
		PW, WAVE, GATE, PITCH, FILTER
	}

	public enum NoteMode {
		NONE, ABS, REL
	}

	public enum TriState {
		UNSET, OFF, ON
	}

	public static final record TableStepIR(int value, int durationFrames, boolean hold,
			boolean waveSet, NoteMode noteMode, int noteValue,
			TriState gate, TriState ring, TriState sync, boolean reset) {
	}

	public static final record TableIR(String name, TableType type, List<TableStepIR> steps, boolean loop) {
	}

	public enum InstrumentGateMode {
		RETRIGGER, LEGATO
	}

	public enum FilterMode {
		LP(1), BP(2), HP(4);

		public final int mask;

		FilterMode(int mask) {
			this.mask = mask;
		}
	}

	public static final record InstrumentIR(String name, int waveMask, AdsrIR adsr, OptionalInt pw,
			OptionalInt pwMin, OptionalInt pwMax, int pwSweep, Optional<String> waveSeq, Optional<String> pwSeq,
			Optional<String> gateSeq, Optional<String> pitchSeq, int filterModeMask, OptionalInt filterCutoff,
			OptionalInt filterRes, Optional<String> filterSeq, InstrumentGateMode gateMode,
			int gateMin, boolean sync, boolean ring) {
		public boolean hasWave(Wave w) {
			return (waveMask & w.mask) != 0;
		}

		public String waveMaskString() {
			if (waveMask == 0)
				return "(none)";
			StringBuilder sb = new StringBuilder();
			for (Wave w : Wave.values()) {
				if ((waveMask & w.mask) != 0) {
					if (sb.length() > 0)
						sb.append('+');
					sb.append(w.name());
				}
			}
			return sb.toString();
		}

	}

	public static final record TimeSigIR(int numerator, int denominator) {
	}

	public enum VideoSystem {
		PAL, NTSC
	}

	public sealed interface SwingSetting permits SwingOff, SwingPercent {
	}

	public static final record SwingOff() implements SwingSetting {
	}

	public static final record SwingPercent(int percent) implements SwingSetting {
	}

	public enum LengthIR {
		L1(1), L2(2), L4(4), L8(8), L16(16), L32(32), L64(64);

		public final int denom;

		LengthIR(int denom) {
			this.denom = denom;
		}

		public static LengthIR fromDenom(int denom) {
			for (LengthIR l : values())
				if (l.denom == denom)
					return l;
			throw new IllegalArgumentException("Unsupported length L" + denom);
		}
	}

	public enum NoteLetter {
		C(0), D(2), E(4), F(5), G(7), A(9), B(11);

		public final int baseSemitone;

		NoteLetter(int baseSemitone) {
			this.baseSemitone = baseSemitone;
		}
	}

	public enum Accidental {
		FLAT(-1), NATURAL(0), SHARP(+1);

		public final int delta;

		Accidental(int delta) {
			this.delta = delta;
		}
	}

	public static final record PitchIR(NoteLetter letter, Accidental acc, int octave) {
		public int toMidi() {
			int semitone = letter.baseSemitone + acc.delta;
			return (octave + 1) * 12 + semitone; // C4=60 when octave=4
		}
	}

	public static final record VoiceIR(int index, String instrumentName, List<VoiceItemIR> items) {
	}

	public static final record ScoreIR(Optional<String> title, Optional<String> author, Optional<String> released,
			int tempoBpm, Optional<TimeSigIR> timeSig, Optional<VideoSystem> system, SwingSetting defaultSwing,
			Map<String, TableIR> tables, Map<String, InstrumentIR> instruments, Map<Integer, VoiceIR> voices) {
	}

	public sealed interface VoiceItemIR
			permits NoteIR, RestIR, HitIR, SetDefaultLengthIR, SetSwingIR, TieIR, LegatoScopeIR, TupletIR, RepeatIR {
	}

	public static final record NoteIR(PitchIR pitch, Optional<LengthIR> length, boolean dotted) implements VoiceItemIR {
	}

	public static final record RestIR(Optional<LengthIR> length, boolean dotted) implements VoiceItemIR {
	}

	public static final record HitIR(Optional<LengthIR> length, boolean dotted) implements VoiceItemIR {
	}

	public static final record SetDefaultLengthIR(LengthIR length) implements VoiceItemIR {
	}

	public static final record SetSwingIR(SwingSetting swing) implements VoiceItemIR {
	}

	public static final record TieIR() implements VoiceItemIR {
	}

	public static final record LegatoScopeIR(List<VoiceItemIR> items) implements VoiceItemIR {
	}

	public static final record TupletIR(List<VoiceItemIR> items) implements VoiceItemIR {
	}

	public static final record RepeatIR(int times, List<VoiceItemIR> items) implements VoiceItemIR {
	}

	// -------------------------
	// IR Level 2: TimedScore
	// -------------------------
	public enum GateMode {
		RETRIG, HOLD
	}

	public enum TimedType {
		NOTE, REST, NOISE
	}

	public static final record TimedEvent(TimedType type, int durationTicks, OptionalInt pitchMidi,
			Optional<GateMode> gateMode) {
		public static TimedEvent note(int midi, int ticks, GateMode gate) {
			return new TimedEvent(TimedType.NOTE, ticks, OptionalInt.of(midi), Optional.of(gate));
		}

		public static TimedEvent rest(int ticks) {
			return new TimedEvent(TimedType.REST, ticks, OptionalInt.empty(), Optional.empty());
		}

		public static TimedEvent noise(int ticks) {
			return new TimedEvent(TimedType.NOISE, ticks, OptionalInt.empty(), Optional.empty());
		}

		public static TimedEvent noise(int ticks, GateMode gate) {
			return new TimedEvent(TimedType.NOISE, ticks, OptionalInt.empty(), Optional.of(gate));
		}
	}

	public static final record TimedVoice(int index, InstrumentIR instrument, List<TimedEvent> events) {
	}

	public static final record TimedScore(Optional<String> title, Optional<String> author, Optional<String> released,
			int tempoBpm, int ticksPerWhole, SwingSetting defaultSwing, VideoSystem system,
			Map<String, TableIR> tables, Map<Integer, TimedVoice> voices) {
	}

	// -------------------------
	// Resolver: ScoreIR -> TimedScore
	// -------------------------
	public static final class Resolver {
		public static final int DEFAULT_TICKS_PER_WHOLE = 192;

		public static final record Result(TimedScore timedScore, Diagnostics diagnostics) {
		}

		public Result resolve(ScoreIR score) {
			Diagnostics diag = new Diagnostics();
			Map<String, TableIR> tables = score.tables();

			// Minimal sanity; main validation is done in ScoreBuildingListener
			if (score.tempoBpm() < 1 || score.tempoBpm() > 300)
				diag.error("TEMPO out of range 1..300");

			Map<Integer, TimedVoice> timedVoices = new LinkedHashMap<>();
			Map<Integer, Integer> filterModesByVoice = new LinkedHashMap<>();
			for (int v = 1; v <= 3; v++) {
				VoiceIR voice = score.voices().get(v);
				if (voice == null) {
					continue;
				}

				InstrumentIR instr = score.instruments().get(voice.instrumentName());
				if (instr == null) {
					diag.error("VOICE " + v + " references undefined instrument: " + voice.instrumentName());
					continue;
				}
				if (instr.filterModeMask() != 0) {
					filterModesByVoice.put(v, instr.filterModeMask());
				}

				if (instr.pwSeq().isPresent()) {
					String tableName = instr.pwSeq().get();
					TableIR table = tables.get(tableName);
					if (table == null) {
						diag.error("Instrument '" + instr.name() + "': PWSEQ references undefined TABLE: "
								+ tableName);
					} else if (table.type() != TableType.PW) {
						diag.error("Instrument '" + instr.name() + "': PWSEQ references non-PW TABLE: " + tableName);
					}
				}
				if (instr.waveSeq().isPresent()) {
					String tableName = instr.waveSeq().get();
					TableIR table = tables.get(tableName);
					if (table == null) {
						diag.error("Instrument '" + instr.name() + "': WAVESEQ references undefined TABLE: "
								+ tableName);
					} else if (table.type() != TableType.WAVE) {
						diag.error("Instrument '" + instr.name() + "': WAVESEQ references non-wave TABLE: "
								+ tableName);
					}
				}
				if (instr.gateSeq().isPresent()) {
					String tableName = instr.gateSeq().get();
					TableIR table = tables.get(tableName);
					if (table == null) {
						diag.error("Instrument '" + instr.name() + "': GATESEQ references undefined TABLE: "
								+ tableName);
					} else if (table.type() != TableType.GATE) {
						diag.error("Instrument '" + instr.name() + "': GATESEQ references non-gate TABLE: "
								+ tableName);
					}
				}
				if (instr.pitchSeq().isPresent()) {
					String tableName = instr.pitchSeq().get();
					TableIR table = tables.get(tableName);
					if (table == null) {
						diag.error("Instrument '" + instr.name() + "': PITCHSEQ references undefined TABLE: "
								+ tableName);
					} else if (table.type() != TableType.PITCH) {
						diag.error("Instrument '" + instr.name() + "': PITCHSEQ references non-pitch TABLE: "
								+ tableName);
					}
				}
				if (instr.filterSeq().isPresent()) {
					String tableName = instr.filterSeq().get();
					TableIR table = tables.get(tableName);
					if (table == null) {
						diag.error("Instrument '" + instr.name() + "': FILTERSEQ references undefined TABLE: "
								+ tableName);
					} else if (table.type() != TableType.FILTER) {
						diag.error("Instrument '" + instr.name() + "': FILTERSEQ references non-filter TABLE: "
								+ tableName);
					}
				}

				boolean hasPwParams = instr.pw().isPresent() || instr.pwMin().isPresent() || instr.pwMax().isPresent()
						|| instr.pwSweep() != 0 || instr.pwSeq().isPresent();
				if (!instr.hasWave(Wave.PULSE) && hasPwParams) {
					diag.warn("Instrument '" + instr.name() + "': PW/PWM set but wave is " + instr.waveMaskString()
							+ " (ignored by SID).");
				}
				boolean hasFilterParams = instr.filterCutoff().isPresent() || instr.filterRes().isPresent()
						|| instr.filterSeq().isPresent();
				if (instr.filterModeMask() == 0 && hasFilterParams) {
					diag.warn("Instrument '" + instr.name()
							+ "': FILTER is OFF but filter parameters are set (ignored).");
				}

				VoiceContext ctx = new VoiceContext(v, instr, score.defaultSwing(), diag);
				List<TimedEvent> events = resolveVoiceItems(voice.items(), ctx);
				timedVoices.put(v, new TimedVoice(v, instr, events));
			}

			if (filterModesByVoice.size() > 1) {
				StringBuilder sb = new StringBuilder("Multiple FILTER modes across voices: ");
				boolean first = true;
				for (var e : filterModesByVoice.entrySet()) {
					if (!first)
						sb.append(", ");
					sb.append("VOICE ").append(e.getKey()).append('=').append(filterMaskString(e.getValue()));
					first = false;
				}
				sb.append(". SID filter is global; last update wins.");
				diag.warn(sb.toString());
			}

			diag.throwIfErrors();
			VideoSystem system = score.system().orElse(VideoSystem.PAL);
			TimedScore ts = new TimedScore(score.title(), score.author(), score.released(), score.tempoBpm(),
					DEFAULT_TICKS_PER_WHOLE, score.defaultSwing(), system, tables, timedVoices);
			return new Result(ts, diag);
		}

		private static final class VoiceContext {
			final int voiceIndex;
			final InstrumentIR instrument;
			final Diagnostics diag;

			LengthIR defaultLen = LengthIR.L8;
			SwingSetting swing;
			boolean inLegato = false;
			int swingPhase = 0; // for 8ths only

			VoiceContext(int voiceIndex, InstrumentIR instrument, SwingSetting defaultSwing, Diagnostics diag) {
				this.voiceIndex = voiceIndex;
				this.instrument = instrument;
				this.swing = defaultSwing;
				this.diag = diag;
			}
		}

		private List<TimedEvent> resolveVoiceItems(List<VoiceItemIR> items, VoiceContext ctx) {
			List<VoiceItemIR> flattened = new ArrayList<>();
			flatten(items, flattened::add);

			List<TimedEvent> out = new ArrayList<>();
			boolean lastWasNote = false;

			for (int i = 0; i < flattened.size(); i++) {
				VoiceItemIR it = flattened.get(i);

				if (it instanceof SetDefaultLengthIR sl) {
					ctx.defaultLen = sl.length();
					continue;
				}
				if (it instanceof SetSwingIR ss) {
					ctx.swing = ss.swing();
					ctx.swingPhase = 0;
					continue;
				}

				if (it instanceof LegatoScopeIR leg) {
					if (ctx.inLegato) {
						ctx.diag.error("Nested (leg) scopes not allowed (VOICE " + ctx.voiceIndex + ")");
						break;
					}
					boolean prev = ctx.inLegato;
					ctx.inLegato = true;
					out.addAll(resolveVoiceItems(leg.items(), ctx));
					ctx.inLegato = prev;
					lastWasNote = !out.isEmpty() && out.get(out.size() - 1).type() == TimedType.NOTE;
					continue;
				}

				if (it instanceof TupletIR tup) {
					// validated earlier; here we just realize timing
					int base = baseLenTicks(ctx.defaultLen);
					int total = 2 * base;
					List<LogicalEv> logical = extractLogicalEventsFromTupletValidated(tup.items(), ctx);

					if (logical.size() != 3) {
						ctx.diag.error("Internal: Tuplet not 3 events during resolve (VOICE " + ctx.voiceIndex + ")");
						break;
					}

					int each = total / 3;
					int rem = total - each * 3;
					for (int k = 0; k < 3; k++) {
						int ticks = each + (k < rem ? 1 : 0);
						LogicalEv ev = logical.get(k);

						TimedEvent te = switch (ev.type) {
						case NOTE -> TimedEvent.note(ev.pitchMidi, ticks, computeGate(ctx, lastWasNote));
						case REST -> TimedEvent.rest(ticks);
						case NOISE -> TimedEvent.noise(ticks, computeGate(ctx, lastWasNote));
						};
						out.add(te);
						lastWasNote = te.type() == TimedType.NOTE || te.type() == TimedType.NOISE;
						// swing ignored inside tuplets => don't advance swingPhase
					}
					continue;
				}

				if (it instanceof TieIR) {
					// tie validation is done earlier in listener for tuplets;
					// outside tuplets, resolver merges by looking at timed stream + next NoteIR
					if (out.isEmpty() || out.get(out.size() - 1).type() != TimedType.NOTE) {
						ctx.diag.error("Tie '&' without previous NOTE in VOICE " + ctx.voiceIndex);
						break;
					}
					if (i + 1 >= flattened.size() || !(flattened.get(i + 1) instanceof NoteIR nn)) {
						ctx.diag.error("Tie '&' must be followed by NOTE in VOICE " + ctx.voiceIndex);
						break;
					}
					int prevMidi = out.get(out.size() - 1).pitchMidi().orElseThrow();
					int nextMidi = nn.pitch().toMidi();
					if (prevMidi != nextMidi) {
						ctx.diag.error("Tie '&' pitch mismatch in VOICE " + ctx.voiceIndex);
						break;
					}

					int add = resolveDurationTicks(nn.length(), nn.dotted(), ctx, false);
					TimedEvent merged = TimedEvent.note(prevMidi, out.get(out.size() - 1).durationTicks() + add,
							GateMode.HOLD);
					out.set(out.size() - 1, merged);
					i += 1; // consume next note
					lastWasNote = true;
					continue;
				}

				if (it instanceof NoteIR n) {
					if (ctx.instrument.hasWave(Wave.NOISE)) {
						ctx.diag.warn(
								"NOTE in NOISE voice (VOICE " + ctx.voiceIndex + "): pitch may be ignored by backend");
					}
					int ticks = resolveDurationTicks(n.length(), n.dotted(), ctx, true);
					out.add(TimedEvent.note(n.pitch().toMidi(), ticks, computeGate(ctx, lastWasNote)));
					lastWasNote = true;
					advanceSwingPhaseIfRelevant(n.length(), ctx);
					continue;
				}

				if (it instanceof RestIR r) {
					int ticks = resolveDurationTicks(r.length(), r.dotted(), ctx, true);
					out.add(TimedEvent.rest(ticks));
					lastWasNote = false;
					advanceSwingPhaseIfRelevant(r.length(), ctx);
					continue;
				}

				if (it instanceof HitIR h) {
					// X legality is validated earlier in listener; keep internal sanity
					if (!ctx.instrument.hasWave(Wave.NOISE)) {
						ctx.diag.error(
								"Internal: 'X' in non-NOISE instrument during resolve (VOICE " + ctx.voiceIndex + ")");
						break;
					}
					int ticks = resolveDurationTicks(h.length(), h.dotted(), ctx, true);
					out.add(TimedEvent.noise(ticks, computeGate(ctx, lastWasNote)));
					lastWasNote = true;
					advanceSwingPhaseIfRelevant(h.length(), ctx);
					continue;
				}

				if (it instanceof RepeatIR) {
					ctx.diag.error("Internal: RepeatIR should be flattened before resolve");
					break;
				}

				ctx.diag.error("Unsupported item during resolve: " + it.getClass().getSimpleName());
				break;
			}

			ctx.diag.throwIfErrors();
			return out;
		}

		private static GateMode computeGate(VoiceContext ctx, boolean lastWasNote) {
			if (ctx.inLegato)
				return lastWasNote ? GateMode.HOLD : GateMode.RETRIG;
			if (ctx.instrument.gateMode() == InstrumentGateMode.LEGATO)
				return lastWasNote ? GateMode.HOLD : GateMode.RETRIG;
			return GateMode.RETRIG;
		}

		private static String filterMaskString(int mask) {
			if (mask == 0)
				return "OFF";
			StringBuilder sb = new StringBuilder();
			for (FilterMode mode : FilterMode.values()) {
				if ((mask & mode.mask) != 0) {
					if (sb.length() > 0)
						sb.append('+');
					sb.append(mode.name());
				}
			}
			return sb.length() > 0 ? sb.toString() : "OFF";
		}

		private static void flatten(List<VoiceItemIR> items, Consumer<VoiceItemIR> out) {
			for (VoiceItemIR it : items) {
				if (it instanceof RepeatIR rep) {
					for (int k = 0; k < rep.times(); k++)
						flatten(rep.items(), out);
				} else
					out.accept(it);
			}
		}

		private static int baseLenTicks(LengthIR len) {
			return switch (len.denom) {
			case 1 -> DEFAULT_TICKS_PER_WHOLE;
			case 2 -> DEFAULT_TICKS_PER_WHOLE / 2;
			case 4 -> DEFAULT_TICKS_PER_WHOLE / 4;
			case 8 -> DEFAULT_TICKS_PER_WHOLE / 8;
			case 16 -> DEFAULT_TICKS_PER_WHOLE / 16;
			case 32 -> DEFAULT_TICKS_PER_WHOLE / 32;
			case 64 -> DEFAULT_TICKS_PER_WHOLE / 64;
			default -> throw new IllegalArgumentException("Unsupported length L" + len.denom);
			};
		}

		private static int resolveDurationTicks(Optional<LengthIR> explicitLen, boolean dotted, VoiceContext ctx,
				boolean applySwing) {
			LengthIR len = explicitLen.orElse(ctx.defaultLen);
			int ticks = baseLenTicks(len);

			if (dotted)
				ticks += ticks / 2;

			if (applySwing && len == LengthIR.L8 && ctx.swing instanceof SwingPercent sp) {
				if (dotted) {
					ctx.diag.warn("Dotted 8th with swing: swing ignored for this event (VOICE " + ctx.voiceIndex + ")");
					return Math.max(1, ticks);
				}
				int totalPair = (DEFAULT_TICKS_PER_WHOLE / 8) * 2; // 48
				int first = Math.round(totalPair * (sp.percent() / 100.0f));
				int second = totalPair - first;
				ticks = (ctx.swingPhase == 0) ? first : second;
			}

			return Math.max(1, ticks);
		}

		private static void advanceSwingPhaseIfRelevant(Optional<LengthIR> explicitLen, VoiceContext ctx) {
			LengthIR len = explicitLen.orElse(ctx.defaultLen);
			if (len == LengthIR.L8 && ctx.swing instanceof SwingPercent)
				ctx.swingPhase ^= 1;
		}

		// Tuplet extraction (validated earlier, but we keep a minimal extractor)
		private enum LogicalType {
			NOTE, REST, NOISE
		}

		private static final class LogicalEv {
			final LogicalType type;
			final int pitchMidi;

			LogicalEv(LogicalType type, int pitchMidi) {
				this.type = type;
				this.pitchMidi = pitchMidi;
			}

			static LogicalEv note(int midi) {
				return new LogicalEv(LogicalType.NOTE, midi);
			}

			static LogicalEv rest() {
				return new LogicalEv(LogicalType.REST, 0);
			}

			static LogicalEv noise() {
				return new LogicalEv(LogicalType.NOISE, 0);
			}
		}

		private List<LogicalEv> extractLogicalEventsFromTupletValidated(List<VoiceItemIR> items, VoiceContext ctx) {
			List<LogicalEv> out = new ArrayList<>();
			for (int i = 0; i < items.size(); i++) {
				VoiceItemIR it = items.get(i);
				if (it instanceof TieIR) {
					// listener should already have validated tie structure in tuplets
					// consume next note
					i++;
					continue;
				}
				if (it instanceof NoteIR n)
					out.add(LogicalEv.note(n.pitch().toMidi()));
				else if (it instanceof RestIR)
					out.add(LogicalEv.rest());
				else if (it instanceof HitIR)
					out.add(LogicalEv.noise());
				else {
					ctx.diag.error("Internal: unsupported item in validated tuplet");
					return List.of();
				}
			}
			return out;
		}
	}
}
