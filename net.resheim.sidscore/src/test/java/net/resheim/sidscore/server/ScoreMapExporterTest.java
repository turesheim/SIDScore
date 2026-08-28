/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import net.resheim.sidscore.ir.SIDScoreIR;
import net.resheim.sidscore.ir.ScoreBuildingListener;
import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;

public class ScoreMapExporterTest {
	private static final Path SOURCE_PATH = Path.of("inline-tune.sidscore").toAbsolutePath().normalize();
	private static final String SOURCE_URI = SOURCE_PATH.toUri().toString();

	private static final String INLINE_TUNE_SOURCE = """
			TITLE "Inline Tune Map Test"
			TEMPO 120
			SYSTEM PAL

			INSTR lead WAVE=PULSE ADSR=0,4,10,4 PW=$0800

			VOICE 1 lead:
			  O4 L8 C D

			TUNE 2 {
			  VOICE 1 lead:
			    O4 L8 F G
			}
			""";

	@Test
	public void buildUsesInlineTuneVoiceSpansForSelectedTune() throws Exception {
		ParsedScore parsed = parse(INLINE_TUNE_SOURCE);
		SIDScoreIR.TimedScore tune = resolveInlineTune(parsed.scoreIR(), 2);

		ScoreMapExporter.ScoreMap map = ScoreMapExporter.build(99, parsed.tree(), 2, tune, SOURCE_URI, SOURCE_PATH);

		assertEquals(SOURCE_URI, map.sources().get(0).sourceUri());
		assertCurrentDisplayText("F", map, 0);
		assertCurrentDisplayText("G", map, map.events().get(0).endFrame());
	}

	@Test
	public void buildUsesTopLevelVoiceSpansForFirstTune() throws Exception {
		ParsedScore parsed = parse(INLINE_TUNE_SOURCE);

		ScoreMapExporter.ScoreMap map = ScoreMapExporter.build(100, parsed.tree(), 1, parsed.timedScore(), SOURCE_URI,
				SOURCE_PATH);

		assertCurrentDisplayText("C", map, 0);
		assertCurrentDisplayText("D", map, map.events().get(0).endFrame());
	}

	private static void assertCurrentDisplayText(String expected, ScoreMapExporter.ScoreMap map, long frameIndex) {
		ScoreMapExporter.EventEntry event = eventById(map, map.activeEventId(1, frameIndex));
		assertEquals(expected, event.displayText());
	}

	private static ScoreMapExporter.EventEntry eventById(ScoreMapExporter.ScoreMap map, int eventId) {
		return map.events().stream()
				.filter(event -> event.eventId() == eventId)
				.findFirst()
				.orElseThrow(() -> new AssertionError("Missing event id " + eventId));
	}

	private static SIDScoreIR.TimedScore resolveInlineTune(SIDScoreIR.ScoreIR base, int tuneNumber) {
		SIDScoreIR.SongIR song = base.songs().get(tuneNumber);
		assertNotNull("Missing inline tune " + tuneNumber, song);
		Map<String, SIDScoreIR.EffectIR> effects = new LinkedHashMap<>();
		if (!song.effects().isEmpty()) {
			effects.putAll(song.effects());
		} else {
			effects.putAll(base.effects());
			effects.putAll(song.effects());
		}
		SIDScoreIR.ScoreIR inlineScore = new SIDScoreIR.ScoreIR(
				song.title().isPresent() ? song.title() : base.title(),
				song.author().isPresent() ? song.author() : base.author(),
				song.released().isPresent() ? song.released() : base.released(),
				song.tempoBpm().isPresent() ? song.tempoBpm().getAsInt() : base.tempoBpm(),
				song.timeSig().isPresent() ? song.timeSig() : base.timeSig(),
				song.system().isPresent() ? song.system() : base.system(),
				song.defaultSwing().isPresent() ? song.defaultSwing().get() : base.defaultSwing(),
				base.tables(),
				base.instruments(),
				effects,
				song.voices(),
				Map.of(),
				Map.of());
		return new SIDScoreIR.Resolver().resolve(inlineScore).timedScore();
	}

	private static ParsedScore parse(String sourceText) {
		SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(sourceText));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SIDScoreParser parser = new SIDScoreParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(new ThrowingErrorListener());

		SIDScoreParser.FileContext tree = parser.file();
		ScoreBuildingListener builder = new ScoreBuildingListener(SOURCE_PATH);
		ParseTreeWalker.DEFAULT.walk(builder, tree);
		SIDScoreIR.ScoreIR scoreIR = builder.buildScoreIR();
		SIDScoreIR.TimedScore timed = new SIDScoreIR.Resolver().resolve(scoreIR).timedScore();
		return new ParsedScore(tree, scoreIR, timed);
	}

	private record ParsedScore(SIDScoreParser.FileContext tree, SIDScoreIR.ScoreIR scoreIR,
			SIDScoreIR.TimedScore timedScore) {
	}

	private static final class ThrowingErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			throw new ScoreBuildingListener.ValidationException(line, charPositionInLine, msg);
		}
	}
}
