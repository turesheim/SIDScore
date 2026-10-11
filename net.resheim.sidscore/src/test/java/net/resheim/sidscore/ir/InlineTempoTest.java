/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.ir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;

public class InlineTempoTest {
	private static final String HEADER = """
			TEMPO 120
			INSTR lead WAVE=PULSE ADSR=0,4,10,4
			""";

	@Test
	public void laterVoiceChangesTempoDuringEarlierVoicesHeldNote() {
		var score = resolve("VOICE 1 lead: C2\nVOICE 2 lead: R4 TEMPO 60 D4");
		assertEquals(List.of(new SIDScoreIR.TempoChangeIR(48, 60)), score.tempoChanges());
		assertEquals(1.5, score.durationSeconds(0, 96), 1e-9);
		assertEquals(1.0, score.durationSeconds(48, 48), 1e-9);
		for (var voice : score.voices().values()) {
			assertEquals(76, FrameEventCompiler.compileVoice(voice, score).stream()
					.mapToInt(FrameEventCompiler.FrameEvent::frames).sum()); // 75 music frames + gate off
		}
	}

	@Test
	public void tempoInsideTieDoesNotRetriggerHeldNote() {
		var score = resolve("VOICE 1 lead: C4 TEMPO 60 & C4");
		var voice = score.voices().get(1);
		assertEquals(1, voice.events().size());
		assertEquals(96, voice.events().get(0).durationTicks());
		assertEquals(SIDScoreIR.GateMode.RETRIG, voice.events().get(0).gateMode().orElseThrow());
		assertEquals(75, FrameEventCompiler.compileVoice(voice, score).get(0).frames());
	}

	@Test
	public void splittingANoteAtTempoChangePreservesItsInitialAttack() {
		var score = resolve("VOICE 1 lead: C4 C4 TEMPO 60 & C4");
		var frames = FrameEventCompiler.compileVoice(score.voices().get(1), score);
		assertEquals(25, frames.get(0).frames());
		assertEquals(1, frames.get(1).frames());
		assertEquals(0, frames.get(1).ctrl() & 1); // Retrigger at the second note's original onset.
		assertEquals(74, frames.get(2).frames()); // No retrigger at the tempo marker inside it.
		assertEquals(1, frames.get(2).ctrl() & 1);
	}

	@Test
	public void repeatsLegatoTupletsAndRestsPlaceMarkersOnMusicalTimeline() {
		var score = resolve("VOICE 1 lead: ( C8 TEMPO 60 D8 TEMPO 120 )x2 "
				+ "(leg) E4 TEMPO 90 F4 (end) L8 T{ G A B } R4 TEMPO 150 C4");
		assertEquals(List.of(new SIDScoreIR.TempoChangeIR(24, 60),
				new SIDScoreIR.TempoChangeIR(48, 120), new SIDScoreIR.TempoChangeIR(72, 60),
				new SIDScoreIR.TempoChangeIR(96, 120), new SIDScoreIR.TempoChangeIR(144, 90),
				new SIDScoreIR.TempoChangeIR(288, 150)), score.tempoChanges());
	}

	@Test
	public void identicalMarkersCanBeSharedButConflictsAreRejected() {
		var score = resolve("VOICE 1 lead: C4 TEMPO 60 D4\nVOICE 2 lead: E4 TEMPO 60 F4");
		assertEquals(1, score.tempoChanges().size());
		assertThrows(IllegalStateException.class,
				() -> resolve("VOICE 1 lead: C4 TEMPO 60 D4\nVOICE 2 lead: E4 TEMPO 90 F4"));
	}

	@Test
	public void tempoRangeAndTupletRestrictionsAreValidated() {
		for (String items : List.of("TEMPO 0 C4", "TEMPO 301 C4", "T{ C TEMPO 60 D E }")) {
			assertThrows(ScoreBuildingListener.ValidationException.class, () -> resolve("VOICE 1 lead: " + items));
		}
	}

	@Test
	public void inlineTuneHasItsOwnTempoChangesAndInheritsOnlyInitialTempo() {
		var base = parse("VOICE 1 lead: C4 TEMPO 60 D4\nTUNE 2 { VOICE 1 lead: E4 TEMPO 90 F4 }");
		var song = base.songs().get(2);
		var tune = new SIDScoreIR.ScoreIR(base.title(), base.author(), base.released(),
				base.tempoBpm(), base.timeSig(), base.system(), base.defaultSwing(), base.tables(),
				base.instruments(), base.effects(), song.voices(), Map.of(), Map.of());
		var resolved = new SIDScoreIR.Resolver().resolve(tune).timedScore();
		assertEquals(List.of(new SIDScoreIR.TempoChangeIR(48, 90)), resolved.tempoChanges());
		assertEquals(7.0 / 6.0, resolved.durationSeconds(0, 96), 1e-9);
		assertEquals(1.5, new SIDScoreIR.Resolver().resolve(base).timedScore().durationSeconds(0, 96), 1e-9);
	}

	private static SIDScoreIR.TimedScore resolve(String body) {
		return new SIDScoreIR.Resolver().resolve(parse(body)).timedScore();
	}

	private static SIDScoreIR.ScoreIR parse(String body) {
		var parser = new SIDScoreParser(new CommonTokenStream(new SIDScoreLexer(CharStreams.fromString(HEADER + body))));
		var tree = parser.file();
		assertEquals(0, parser.getNumberOfSyntaxErrors());
		var builder = new ScoreBuildingListener(Path.of("inline-tempo.sidscore").toAbsolutePath());
		ParseTreeWalker.DEFAULT.walk(builder, tree);
		return builder.buildScoreIR();
	}
}
