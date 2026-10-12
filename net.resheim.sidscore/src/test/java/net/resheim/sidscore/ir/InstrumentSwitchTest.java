/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.ir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;
import net.resheim.sidscore.sid.SidModel;
import net.resheim.sidscore.sid.SidWaveforms;

public class InstrumentSwitchTest {
	private static final String HEADER = """
			TEMPO 120
			INSTR lead WAVE=PULSE ADSR=0,4,10,4 PW=$0800
			INSTR bass WAVE=SAW ADSR=2,3,8,5 GATE=LEGATO
			INSTR drum WAVE=NOISE ADSR=0,0,15,0
			""";

	@Test
	public void changesPersistThroughRepeatsAndLegatoWithoutConsumingTime() {
		var score = resolve("VOICE 1 lead: L4 ( C INSTR=bass D INSTR=lead )x2 "
				+ "(leg) E INSTR=bass F G (end) A\nVOICE 2 lead: B");
		var events = score.voices().get(1).events();
		assertEquals(List.of("lead", "bass", "lead", "bass", "lead", "bass", "bass", "bass"),
				events.stream().map(e -> e.instrument().orElseThrow().name()).toList());
		assertEquals(384, events.stream().mapToInt(SIDScoreIR.TimedEvent::durationTicks).sum());
		assertEquals("lead", score.voices().get(2).events().get(0).instrument().orElseThrow().name());
		var frames = FrameEventCompiler.compileVoice(score.voices().get(1), score);
		assertTrue(frames.stream().anyMatch(e -> e.instrument() != null && e.instrument().name().equals("bass")
				&& (e.ctrl() & 0xf0) == 0x20));
		// Instrument changes force an attack even inside legato.
		assertEquals(0, frames.get(1).ctrl() & 1);
		assertEquals("bass", frames.get(1).instrument().name());
	}

	@Test
	public void validatesNoiseAgainstActiveInstrumentIncludingRepeatIterations() {
		resolve("VOICE 1 lead: C INSTR=drum X INSTR=lead D");
		resolve("VOICE 1 drum: ( INSTR=lead C INSTR=drum X )x2");
		assertThrows(IllegalStateException.class, () -> resolve("VOICE 1 drum: X INSTR=lead X"));
		assertThrows(IllegalStateException.class, () -> resolve("VOICE 1 drum: ( X INSTR=lead C )x2"));
		assertThrows(IllegalStateException.class, () -> resolve("VOICE 1 lead: INSTR=missing C"));
	}

	@Test
	public void rejectsTiesAcrossChangesAndStateChangesInTuplets() {
		assertThrows(IllegalStateException.class, () -> resolve("VOICE 1 lead: C INSTR=bass & C"));
		assertThrows(ScoreBuildingListener.ValidationException.class,
				() -> resolve("VOICE 1 lead: T{ C INSTR=bass D E }"));
		assertEquals(1, resolve("VOICE 1 lead: C INSTR=lead & C").voices().get(1).events().size());
	}

	@Test
	public void instrumentDefinitionAfterVoiceIsUnambiguousAndForwardReferencesWork() {
		var score = resolve("VOICE 1 lead: C INSTR=later D\nINSTR later WAVE=TRI ADSR=0,0,15,0");
		assertEquals("later", score.voices().get(1).events().get(1).instrument().orElseThrow().name());
	}

	@Test
	public void realtimeRuntimeLoadsAndRestoresInstrumentAndTables() {
		var score = resolve("TABLE wave shape { TRI HOLD }\n"
				+ "INSTR shaped WAVE=SAW ADSR=0,0,15,0 WAVESEQ=shape PW=$0200\n"
				+ "VOICE 1 lead: C4 INSTR=shaped D4 INSTR=lead E4");
		var voice = score.voices().get(1);
		var runtime = new RealtimeAudioPlayer.VoiceRuntime(1, voice.instrument(),
				FrameEventCompiler.compileVoice(voice, score), 985248.0, 50.124542, score.tables(),
				new RealtimeAudioPlayer.FilterRuntime(50.124542), SidWaveforms.loadTables(SidModel.MOS6581, null), null);
		for (int frame = 0; frame < 75; frame++) {
			float sampleRate = (float) 50.124542;
			runtime.prepareSample(sampleRate);
			runtime.advanceOsc(sampleRate);
			runtime.renderSample(sampleRate, false);
			if (frame == 0 || frame == 26 || frame == 51) {
				assertEquals(frame == 26 ? SIDScoreIR.Wave.TRI.mask : SIDScoreIR.Wave.PULSE.mask,
						runtime.snapshot(1).waveMask());
				assertEquals(frame == 26 ? 0x0200 : 0x0800, runtime.snapshot(1).pulseWidth());
			}
		}
	}

	@Test
	public void switchedInstrumentTablesAndGateMinimumAreValidatedAndCompiled() {
		assertThrows(IllegalStateException.class, () -> resolve(
				"INSTR bad WAVE=PULSE ADSR=0,0,15,0 WAVESEQ=missing\nVOICE 1 lead: C INSTR=bad D"));
		var score = resolve("INSTR slow WAVE=TRI ADSR=0,0,15,0 GATEMIN=30 SYNC=ON RING=ON\n"
				+ "VOICE 1 lead: C64 INSTR=slow D64");
		var frames = FrameEventCompiler.compileVoice(score.voices().get(1), score);
		assertEquals(30, frames.stream().filter(e -> e.instrument() != null && e.instrument().name().equals("slow"))
				.mapToInt(FrameEventCompiler.FrameEvent::frames).sum());
		assertEquals(0x17, frames.get(2).ctrl());
	}

	@Test
	public void tuneInstrumentSelectionsAreIndependent() {
		var score = parse("VOICE 1 lead: C INSTR=bass D\nTUNE 2 { VOICE 1 lead: E INSTR=drum X }");
		var tune = new SIDScoreIR.ScoreIR(score.title(), score.author(), score.released(), score.tempoBpm(),
				score.timeSig(), score.system(), score.defaultSwing(), score.tables(), score.instruments(),
				score.effects(), score.songs().get(2).voices(), Map.of(), Map.of());
		assertEquals("drum", new SIDScoreIR.Resolver().resolve(tune).timedScore().voices().get(1)
				.events().get(1).instrument().orElseThrow().name());
	}

	private static SIDScoreIR.TimedScore resolve(String body) {
		return new SIDScoreIR.Resolver().resolve(parse(body)).timedScore();
	}

	private static SIDScoreIR.ScoreIR parse(String body) {
		var parser = new SIDScoreParser(new CommonTokenStream(new SIDScoreLexer(CharStreams.fromString(HEADER + body))));
		var tree = parser.file();
		assertEquals(0, parser.getNumberOfSyntaxErrors());
		var builder = new ScoreBuildingListener(Path.of("instrument-switch.sidscore").toAbsolutePath());
		ParseTreeWalker.DEFAULT.walk(builder, tree);
		return builder.buildScoreIR();
	}
}
