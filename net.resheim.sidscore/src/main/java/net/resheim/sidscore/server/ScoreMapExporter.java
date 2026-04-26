/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.server;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Token;

import net.resheim.sidscore.ir.SIDScoreIR;
import net.resheim.sidscore.parser.SIDScoreParser;

public final class ScoreMapExporter {
	private static final double RASTER_RATE_PAL = 50.124542;
	private static final double RASTER_RATE_NTSC = 60.098814;
	private static final int RETRIG_GAP_FRAMES = 1;

	public record SourceEntry(int sourceId, String sourceUri, Path sourcePath) {
	}

	public record EventEntry(int eventId, int voiceIndex, int noteKind, int flags, long startFrame, long endFrame,
			int sourceId, int startLine, int startColumn, int endLine, int endColumn, String displayText) {
	}

	public record ScoreMap(long scoreId, List<SourceEntry> sources, List<EventEntry> events,
			Map<Integer, List<EventEntry>> eventsByVoice) {
		public int activeEventId(int voiceIndex, long frameIndex) {
			List<EventEntry> voiceEvents = eventsByVoice.get(voiceIndex);
			if (voiceEvents == null) {
				return -1;
			}
			for (EventEntry ev : voiceEvents) {
				if (frameIndex >= ev.startFrame() && frameIndex < ev.endFrame()) {
					return ev.eventId();
				}
			}
			return -1;
		}
	}

	private ScoreMapExporter() {
	}

	public static ScoreMap build(long scoreId, SIDScoreParser.FileContext tree, SIDScoreIR.TimedScore timed,
			String sourceUri, Path sourcePath) {
		List<SourceEntry> sources = List.of(new SourceEntry(1, sourceUri, sourcePath));
		List<EventEntry> events = new ArrayList<>();
		Map<Integer, List<EventEntry>> byVoice = new LinkedHashMap<>();
		int[] nextEventId = { 1 };

		for (SIDScoreParser.StmtContext stmt : tree.stmt()) {
			if (stmt.voiceBlock() == null) {
				continue;
			}
			SIDScoreParser.VoiceBlockContext voiceCtx = stmt.voiceBlock();
			int voiceIndex = Integer.parseInt(voiceCtx.INT().getText());
			if (voiceIndex < 1 || voiceIndex > 3) {
				continue;
			}
			SIDScoreIR.TimedVoice timedVoice = timed.voices().get(voiceIndex);
			if (timedVoice == null) {
				continue;
			}
			List<TokenSpan> spans = collectEventSpans(voiceCtx.voiceItem());
			List<EventEntry> voiceEvents = buildVoiceEvents(nextEventId, timed, timedVoice, spans);
			events.addAll(voiceEvents);
			byVoice.put(voiceIndex, voiceEvents);
		}

		return new ScoreMap(scoreId, sources, List.copyOf(events), copyEventMap(byVoice));
	}

	private static Map<Integer, List<EventEntry>> copyEventMap(Map<Integer, List<EventEntry>> source) {
		Map<Integer, List<EventEntry>> out = new LinkedHashMap<>();
		for (var entry : source.entrySet()) {
			out.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		return Map.copyOf(out);
	}

	private static List<EventEntry> buildVoiceEvents(int[] nextEventId, SIDScoreIR.TimedScore score,
			SIDScoreIR.TimedVoice voice, List<TokenSpan> spans) {
		List<EventEntry> out = new ArrayList<>();
		double ticksPerQuarter = score.ticksPerWhole() / 4.0;
		double secondsPerTick = 60.0 / score.tempoBpm() / ticksPerQuarter;
		double frameRate = score.system() == SIDScoreIR.VideoSystem.PAL ? RASTER_RATE_PAL : RASTER_RATE_NTSC;
		int gateMin = Math.max(0, voice.instrument().gateMin());
		double rem = 0.0;
		long frameCursor = 0;
		boolean lastGateOn = false;
		List<SIDScoreIR.TimedEvent> events = voice.events();

		for (int i = 0; i < events.size(); i++) {
			SIDScoreIR.TimedEvent ev = events.get(i);
			TokenSpan span = i < spans.size() ? spans.get(i) : TokenSpan.approximate();
			double framesExact = ev.durationTicks() * secondsPerTick * frameRate + rem;
			int frames = (int) Math.max(1, Math.round(framesExact));
			rem = framesExact - frames;

			if ((ev.type() == SIDScoreIR.TimedType.NOTE || ev.type() == SIDScoreIR.TimedType.NOISE) && gateMin > 0) {
				frames = Math.max(frames, gateMin);
			}

			int noteKind = noteKind(ev);
			int baseFlags = span.approximate ? 1 << 4 : 0;
			if (ev.gateMode().orElse(SIDScoreIR.GateMode.RETRIG) == SIDScoreIR.GateMode.HOLD) {
				baseFlags |= 1 << 1;
			}

			if ((ev.type() == SIDScoreIR.TimedType.NOTE || ev.type() == SIDScoreIR.TimedType.NOISE)) {
				SIDScoreIR.GateMode gate = ev.gateMode().orElse(SIDScoreIR.GateMode.RETRIG);
				if (gate == SIDScoreIR.GateMode.RETRIG && lastGateOn && frames > RETRIG_GAP_FRAMES) {
					out.add(event(nextEventId[0]++, voice.index(), noteKind, baseFlags | (1 << 2), frameCursor,
							frameCursor + RETRIG_GAP_FRAMES, span, displayText(ev, span)));
					frameCursor += RETRIG_GAP_FRAMES;
					frames -= RETRIG_GAP_FRAMES;
				}
				baseFlags |= 1;
				lastGateOn = true;
			} else {
				lastGateOn = false;
			}

			while (frames > 0) {
				int chunk = Math.min(frames, 0xFFFF);
				out.add(event(nextEventId[0]++, voice.index(), noteKind, baseFlags, frameCursor, frameCursor + chunk,
						span, displayText(ev, span)));
				frameCursor += chunk;
				frames -= chunk;
			}
		}

		return out;
	}

	private static EventEntry event(int eventId, int voiceIndex, int noteKind, int flags, long startFrame,
			long endFrame, TokenSpan span, String displayText) {
		return new EventEntry(eventId, voiceIndex, noteKind, flags, startFrame, endFrame, 1, span.startLine,
				span.startColumn, span.endLine, span.endColumn, displayText);
	}

	private static int noteKind(SIDScoreIR.TimedEvent ev) {
		return switch (ev.type()) {
		case NOTE -> 1;
		case NOISE -> 2;
		case REST -> 0;
		};
	}

	private static String displayText(SIDScoreIR.TimedEvent ev, TokenSpan span) {
		if (!span.text.isBlank()) {
			return span.text;
		}
		return switch (ev.type()) {
		case NOTE -> "NOTE";
		case NOISE -> "NOISE";
		case REST -> "REST";
		};
	}

	private static List<TokenSpan> collectEventSpans(List<SIDScoreParser.VoiceItemContext> items) {
		TieState tie = new TieState();
		List<TokenSpan> out = new ArrayList<>();
		appendEventSpans(items, out, tie, false);
		return out;
	}

	private static void appendEventSpans(List<SIDScoreParser.VoiceItemContext> items, List<TokenSpan> out,
			TieState tie, boolean allowTieCarry) {
		for (SIDScoreParser.VoiceItemContext item : items) {
			if (item.AMP() != null) {
				tie.skipNextNote = true;
				continue;
			}
			if (item.noteOrRestOrHit() != null) {
				SIDScoreParser.NoteOrRestOrHitContext n = item.noteOrRestOrHit();
				Token tok = null;
				boolean isNote = false;
				if (n.NOTE() != null) {
					tok = n.NOTE().getSymbol();
					isNote = true;
				} else if (n.REST() != null) {
					tok = n.REST().getSymbol();
				} else if (n.HIT() != null) {
					tok = n.HIT().getSymbol();
				}
				if (tok != null) {
					if (isNote && tie.skipNextNote) {
						tie.skipNextNote = false;
					} else {
						out.add(TokenSpan.of(tok));
					}
				}
				continue;
			}
			if (item.legatoScope() != null) {
				appendEventSpans(item.legatoScope().voiceItem(), out, tie, true);
				continue;
			}
			if (item.tuplet() != null) {
				appendEventSpans(item.tuplet().voiceItem(), out, tie, true);
				continue;
			}
			if (item.repeat() != null) {
				int times = parseRepeatTimes(item.repeat().REPEAT_END().getText());
				for (int i = 0; i < times; i++) {
					TieState innerTie = new TieState();
					appendEventSpans(item.repeat().voiceItem(), out, innerTie, true);
				}
				if (!allowTieCarry) {
					tie.skipNextNote = false;
				}
			}
		}
	}

	private static int parseRepeatTimes(String tokenText) {
		int idx = tokenText.lastIndexOf('x');
		if (idx >= 0 && idx + 1 < tokenText.length()) {
			try {
				return Integer.parseInt(tokenText.substring(idx + 1));
			} catch (NumberFormatException ignored) {
				return 1;
			}
		}
		return 1;
	}

	private static final class TieState {
		private boolean skipNextNote = false;
	}

	private static final class TokenSpan {
		final int startLine;
		final int startColumn;
		final int endLine;
		final int endColumn;
		final String text;
		final boolean approximate;

		private TokenSpan(int startLine, int startColumn, int endLine, int endColumn, String text,
				boolean approximate) {
			this.startLine = startLine;
			this.startColumn = startColumn;
			this.endLine = endLine;
			this.endColumn = endColumn;
			this.text = text;
			this.approximate = approximate;
		}

		static TokenSpan of(Token token) {
			String text = token.getText();
			int startLine = Math.max(1, token.getLine());
			int startColumn = Math.max(1, token.getCharPositionInLine() + 1);
			int endColumn = startColumn + (text != null ? text.length() : 0);
			return new TokenSpan(startLine, startColumn, startLine, Math.max(startColumn, endColumn),
					text != null ? text : "", false);
		}

		static TokenSpan approximate() {
			return new TokenSpan(1, 1, 1, 1, "", true);
		}
	}
}
