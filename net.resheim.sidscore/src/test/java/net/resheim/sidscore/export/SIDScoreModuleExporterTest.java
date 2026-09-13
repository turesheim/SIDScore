/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SIDScoreModuleExporterTest {
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void generatedModuleAssemblesAtTwoHostAddresses() throws Exception {
		Path root = repositoryRoot();
		Path source = root.resolve("examples/test.sidscore");
		Path kickAss = root.resolve("net.resheim.sidscore/lib/KickAss.jar");
		assertTrue(Files.isRegularFile(source));
		assertTrue(Files.isRegularFile(kickAss));

		Path directory = temporaryFolder.getRoot().toPath();
		String module = new SIDScoreModuleExporter().generate(source, "Score");
		Files.writeString(directory.resolve("score.asm"), module, StandardCharsets.US_ASCII);
		byte[] at3000 = assemble(directory, kickAss, 0x3000);
		byte[] at4000 = assemble(directory, kickAss, 0x4000);

		assertEquals(at3000.length, at4000.length);
		assertEquals(0x3000, word(at3000, 0));
		assertEquals(0x4000, word(at4000, 0));
		assertEquals(0x20, at3000[2] & 0xff); // JSR Score.init
		assertEquals(0x20, at3000[5] & 0xff); // JSR Score.play
		assertEquals(0x1000, word(at4000, 3) - word(at3000, 3));
		assertEquals(0x1000, word(at4000, 6) - word(at3000, 6));
	}

	@Test
	public void inlineTunesAndEffectsAssembleWithCallableTriggers() throws Exception {
		Path root = repositoryRoot();
		Path source = root.resolve("examples/sfx/effects.sidscore");
		Path kickAss = root.resolve("net.resheim.sidscore/lib/KickAss.jar");
		String module = new SIDScoreModuleExporter().generate(source, "Score");
		assertTrue(module.contains(".namespace tune1 {"));
		assertTrue(module.contains(".namespace tune2 {"));
		assertTrue(module.contains("effect_Blip:"));
		assertTrue(module.contains("effect_NoiseZap:"));
		assertTrue(module.contains("sfx_apply:"));
		assertTrue(module.contains("sta $d400,x"));
		assertTrue(module.contains("sta $d417"));
		assertTrue(module.contains("sta $d418"));
		assertTrue(module.contains("and sfx_shadow417"));
		String tune2 = module.substring(module.indexOf(".namespace tune2 {"));
		String frames = tune2.substring(tune2.indexOf("sfx_data_1:\n"));
		assertTrue(frames.contains(".byte $00,$40,$00,$08,$81,$04,$82")); // NoiseZap starts on voice 3
		assertTrue(frames.contains(".byte $00,$30,$00,$08,$81,$04,$82")); // tick 1 frequency
		assertTrue(frames.contains(".byte $00,$20,$00,$08,$81,$04,$82")); // tick 2 frequency
		assertTrue(frames.contains("$f0,$80,$7f,$3c")); // filter resonance/mode and volume at tick 1
		Path directory = temporaryFolder.getRoot().toPath();
		Files.writeString(directory.resolve("score.asm"), module, StandardCharsets.US_ASCII);
		byte[] prg = assembleHost(directory, kickAss, "inline-effects", """
				*=$3000 "HOST"
				start:
				  lda #1
				  jsr Score.init
				  jsr Score.tune1.effect_Blip
				  jsr Score.tune2.effect_NoiseZap
				  jsr Score.play
				  lda #2
				  jsr Score.init
				  jsr Score.play
				  rts
				#import "score.asm"
				""");
		assertEquals(0x3000, word(prg, 0));
		assertTrue(contains(prg, new int[] { 0x9d, 0x00, 0xd4 })); // STA $d400,X in effect renderer
		assertTrue(contains(prg, new int[] { 0x8d, 0x17, 0xd4 })); // STA $d417
		assertTrue(contains(prg, new int[] { 0x8d, 0x18, 0xd4 })); // STA $d418
	}

	@Test
	public void importedEffectTuneAssembles() throws Exception {
		Path root = repositoryRoot();
		Path directory = temporaryFolder.getRoot().toPath();
		Path source = directory.resolve("root.sidscore");
		Path imported = directory.resolve("effect.sidscore");
		Files.writeString(source, """
				TITLE "Base"
				TEMPO 120
				TIME 4/4
				SYSTEM PAL
				INSTR lead WAVE=TRI ADSR=0,0,15,0
				VOICE 1 lead: O4 C4
				IMPORT "effect.sidscore" AS 2
				""");
		Files.writeString(imported, """
				TITLE "Effect"
				TEMPO 120
				TIME 4/4
				SYSTEM PAL
				EFFECT Zap {
				  VOICE 3
				  LENGTH 3 TICKS
				  WAVE=NOISE
				  ADSR=0,0,15,0
				  GATE=ON
				  FREQ=$2000
				  GATE=OFF @2
				}
				""");
		String module = new SIDScoreModuleExporter().generate(source, "Score");
		assertTrue(module.contains(".namespace tune2 {"));
		assertTrue(module.contains("effect_Zap:"));
		assertTrue(module.contains("jsr effects_start")); // selected effect-only tune auto-triggers on init
		Files.writeString(directory.resolve("score.asm"), module, StandardCharsets.US_ASCII);
		assembleHost(directory, root.resolve("net.resheim.sidscore/lib/KickAss.jar"), "imported-effects", """
				*=$3000 "HOST"
				start:
				  lda #1
				  jsr Score.init
				  jsr Score.tune2.effect_Zap
				  jsr Score.play
				  lda #2
				  jsr Score.init
				  jsr Score.play
				  rts
				#import "score.asm"
				""");
	}

	private Path repositoryRoot() {
		Path root = Path.of("").toAbsolutePath().normalize();
		return Files.exists(root.resolve("examples/test.sidscore")) ? root : root.getParent();
	}

	private byte[] assemble(Path directory, Path kickAss, int address) throws Exception {
		String stem = Integer.toHexString(address);
		Path host = directory.resolve("host-" + stem + ".asm");
		Path program = directory.resolve("host-" + stem + ".prg");
		Files.writeString(host, "*=$" + stem + " \"HOST\"\n"
				+ "start:\n  jsr Score.init\n  jsr Score.play\n  rts\n"
				+ "#import \"score.asm\"\n", StandardCharsets.US_ASCII);
		Path java = Path.of(System.getProperty("java.home"), "bin", "java");
		Process process = new ProcessBuilder(java.toString(), "-jar", kickAss.toString(),
				host.toString(), "-o", program.toString()).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertEquals(output, 0, process.waitFor());
		return Files.readAllBytes(program);
	}

	private byte[] assembleHost(Path directory, Path kickAss, String stem, String hostSource) throws Exception {
		Path host = directory.resolve(stem + ".asm");
		Path program = directory.resolve(stem + ".prg");
		Files.writeString(host, hostSource, StandardCharsets.US_ASCII);
		Path java = Path.of(System.getProperty("java.home"), "bin", "java");
		Process process = new ProcessBuilder(java.toString(), "-jar", kickAss.toString(),
				host.toString(), "-o", program.toString()).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertEquals(output, 0, process.waitFor());
		return Files.readAllBytes(program);
	}

	private boolean contains(byte[] bytes, int[] pattern) {
		outer: for (int offset = 0; offset <= bytes.length - pattern.length; offset++) {
			for (int i = 0; i < pattern.length; i++) {
				if ((bytes[offset + i] & 0xff) != pattern[i]) continue outer;
			}
			return true;
		}
		return false;
	}

	private int word(byte[] bytes, int offset) {
		return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
	}
}
