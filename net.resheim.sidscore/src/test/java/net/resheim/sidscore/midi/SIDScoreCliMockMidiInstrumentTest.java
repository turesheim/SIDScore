/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.midi;

import net.resheim.sidscore.SIDScoreCLI;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifies CLI paths that discover or open Java Sound MIDI input instruments.
 * <p>
 * The mocked instrument is installed at the same seam used by the lower-level
 * router tests. That keeps these tests independent from host hardware while
 * still validating the public command behavior a human runs from a terminal.
 */
public class SIDScoreCliMockMidiInstrumentTest {

	@BeforeClass
	public static void disableMacEventPumpForCliTests() {
		System.setProperty("sidscore.midi.awtEventPump.disabled", "true");
	}

	@Test
	public void listMidiDevicesCommandReportsMockInstrument() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock CLI Keys");

		try (AutoCloseable ignored = MockMidiInstrument.install(input)) {
			String stdout = captureStdout(() -> SIDScoreCLI.main(new String[] { "--list-midi-devices" }));

			assertTrue(stdout.contains("Available MIDI input devices:"));
			assertTrue(stdout.contains("Mock CLI Keys"));
			assertTrue(stdout.contains("selector='Mock CLI Keys'"));
		}
	}

	@Test
	public void midiProbeCommandOpensAndClosesSelectedMockInstrument() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock Probe Keys");

		try (AutoCloseable ignored = MockMidiInstrument.install(input)) {
			String stdout = captureStdout(() -> SIDScoreCLI.main(new String[] {
					"--midi-probe", "--midi-device", "Mock Probe Keys", "--seconds", "1"
			}));

			assertTrue(stdout.contains("MIDI Probe Input: Mock Probe Keys selector='Mock Probe Keys'"));
			assertEquals("The probe opens one Java Sound transmitter for the selected mock.", 1, input.openCount());
			assertEquals("The probe closes the mock instrument before returning.", 1, input.closeCount());
		}
	}

	@Test
	public void midiProbeAllCommandOpensMockInstrumentByDiscoveredIndex() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock Probe All Keys");

		try (AutoCloseable ignored = MockMidiInstrument.install(input)) {
			String stdout = captureStdout(() -> SIDScoreCLI.main(new String[] {
					"--midi-probe", "--all", "--seconds", "1"
			}));

			assertTrue(stdout.contains("MIDI Probe Input: Mock Probe All Keys selector='0'"));
			assertEquals("The --all probe opens the discovered mock input by index.", 1, input.openCount());
			assertEquals("The --all probe closes the discovered mock before returning.", 1, input.closeCount());
		}
	}

	private static String captureStdout(ThrowingRunnable runnable) throws Exception {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (PrintStream capture = new PrintStream(out, true, StandardCharsets.UTF_8)) {
			System.setOut(capture);
			runnable.run();
		} finally {
			System.setOut(originalOut);
		}
		return out.toString(StandardCharsets.UTF_8);
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
