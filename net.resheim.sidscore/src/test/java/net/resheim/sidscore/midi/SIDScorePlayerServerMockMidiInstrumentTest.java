/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.midi;

import net.resheim.sidscore.SIDScorePlayerServer;
import net.resheim.sidscore.server.SrapProtocol;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Exercises SRAP server MIDI paths with a mocked input instrument.
 * <p>
 * These are intentionally protocol-level tests: they verify that the same
 * {@code SCAN_MIDI_DEVICES} and {@code SET_MIDI_SETTINGS} frames used by Theia
 * can discover, auto-select, and open a MIDI input without relying on a physical
 * keyboard.
 */
public class SIDScorePlayerServerMockMidiInstrumentTest {

	@BeforeClass
	public static void configureFastHeadlessMidiTests() {
		System.setProperty("sidscore.midi.awtEventPump.disabled", "true");
		// The multi-voice restart regression test sends a MIDI note while the
		// server has opened the replacement input source but has not yet reused it
		// in the monitor thread. A short warmup keeps that race deterministic.
		System.setProperty("sidscore.midi.warmupMs", "150");
		// Keep the monitor armed after SET_MIDI_SETTINGS, but do not open audio until
		// a test explicitly sends note input through the mock instrument.
		System.setProperty("sidscore.midi.monitor.startOnInput", "true");
	}

	@Test
	public void scanMidiDevicesReportsAndAutoSelectsSingleMockInstrument() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock SRAP Keys");

		try (AutoCloseable ignored = MockMidiInstrument.install(input);
				ServerHarness server = ServerHarness.start()) {
			server.hello();
			server.send(SrapProtocol.SCAN_MIDI_DEVICES, SrapProtocol.payload().u32(200).toByteArray());

			MidiDeviceList deviceList = MidiDeviceList.read(server.readFrame(SrapProtocol.MIDI_DEVICE_LIST, 200));
			assertEquals(1, deviceList.devices().size());
			assertEquals("Mock SRAP Keys", deviceList.devices().get(0).displayName());
			assertEquals("SIDScore Test", deviceList.devices().get(0).vendor());

			MidiState state = MidiState.read(server.readFrame(SrapProtocol.MIDI_STATE, 200));
			assertFalse("Scanning alone selects the instrument but does not turn global MIDI on.",
					state.enabled());
			assertEquals("Mock SRAP Keys", state.assignment(1).deviceSelector());
			assertTrue("The default voice assignment is prepared for the selected mock instrument.",
					state.assignment(1).voiceEnabled());
		}
	}

	@Test
	public void setMidiSettingsOpensMockInstrumentThroughServerMidiSource() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock Live Keys");

		try (AutoCloseable ignored = MockMidiInstrument.install(input);
				ServerHarness server = ServerHarness.start()) {
			server.hello();
			server.send(SrapProtocol.SET_MIDI_SETTINGS, midiSettingsPayload(201, true,
					new MidiAssignment(1, true, 1, "Mock Live Keys")));

			MidiState state = MidiState.read(server.readFrame(SrapProtocol.MIDI_STATE, 201));
			assertTrue(state.enabled());
			assertEquals("Mock Live Keys", state.assignment(1).deviceSelector());
			assertEquals("Mock Live Keys", state.assignment(1).deviceName());

			server.waitUntil(input::hasReceiver);
			assertEquals("The SRAP server opens exactly one Java Sound transmitter for voice 1.",
					1, input.openCount());
			assertTrue(input.isOpen());
		}
	}

	@Test
	public void addingSecondMidiVoiceStillLetsServerDetectMockInput() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock Multi Voice Keys");

		try (CapturedStdout stdout = CapturedStdout.start();
				AutoCloseable ignored = MockMidiInstrument.install(input);
				ServerHarness server = ServerHarness.start()) {
			server.hello();
			server.send(SrapProtocol.SET_MIDI_SETTINGS, midiSettingsPayload(203, true,
					new MidiAssignment(1, true, 1, "Mock Multi Voice Keys")));
			MidiState initial = MidiState.read(server.readFrame(SrapProtocol.MIDI_STATE, 203));
			assertTrue(initial.enabled());
			assertTrue(initial.assignment(1).voiceEnabled());
			server.waitUntil(input::hasReceiver);
			assertEquals("The first MIDI monitor start opens one Java Sound input.",
					1, input.openCount());

			server.send(SrapProtocol.SET_MIDI_SETTINGS, midiSettingsPayload(204, true,
					new MidiAssignment(1, true, 1, "Mock Multi Voice Keys"),
					new MidiAssignment(2, true, 1, "Mock Multi Voice Keys")));
			MidiState updated = MidiState.read(server.readFrame(SrapProtocol.MIDI_STATE, 204));
			assertTrue(updated.enabled());
			assertTrue(updated.assignment(1).voiceEnabled());
			assertTrue(updated.assignment(2).voiceEnabled());

			// The regression happened during this restart window: the server closed
			// and reopened the same physical device while adding voice 2. On macOS
			// that can leave the reopened receiver present but no longer receiving
			// callbacks, so this test requires an in-place remap.
			stdout.awaitContains("remapped MIDI input source without reopening", 2_000);
			assertEquals("Adding voice 2 on the same MIDI device must remap the existing input instead of "
					+ "closing and reopening it; macOS can otherwise leave callbacks detached.",
					1, input.openCount());
			server.waitUntil(input::hasReceiver);
			input.send(MockMidiInstrument.shortMessage(javax.sound.midi.ShortMessage.NOTE_ON, 1, 60, 100));

			// The monitor logs this before opening audio, so the test validates input
			// detection without depending on a host audio device.
			stdout.awaitContains("MIDI monitor input detected", 2_000);
		}
	}

	@Test
	public void disablingAndReenablingMidiReusesMockInput() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Mock Toggle Keys");

		try (CapturedStdout stdout = CapturedStdout.start();
				AutoCloseable ignored = MockMidiInstrument.install(input);
				ServerHarness server = ServerHarness.start()) {
			server.hello();
			server.send(SrapProtocol.SET_MIDI_SETTINGS, midiSettingsPayload(205, true,
					new MidiAssignment(1, true, 1, "Mock Toggle Keys")));
			MidiState initial = MidiState.read(server.readFrame(SrapProtocol.MIDI_STATE, 205));
			assertTrue(initial.enabled());
			server.waitUntil(input::hasReceiver);
			assertEquals(1, input.openCount());

			server.send(SrapProtocol.SET_MIDI_SETTINGS, midiSettingsPayload(206, false,
					new MidiAssignment(1, true, 1, "Mock Toggle Keys")));
			MidiState disabled = MidiState.read(server.readFrame(SrapProtocol.MIDI_STATE, 206));
			assertFalse(disabled.enabled());
			assertTrue(disabled.assignment(1).voiceEnabled());
			stdout.awaitContains("suspended MIDI input source while MIDI is disabled", 2_000);
			assertTrue("Disabling MIDI should keep the Java Sound input open for reuse.", input.isOpen());
			assertEquals("Disabling MIDI should suspend, not close and reopen, the selected input.",
					1, input.openCount());

			server.send(SrapProtocol.SET_MIDI_SETTINGS, midiSettingsPayload(207, true,
					new MidiAssignment(1, true, 1, "Mock Toggle Keys")));
			MidiState reenabled = MidiState.read(server.readFrame(SrapProtocol.MIDI_STATE, 207));
			assertTrue(reenabled.enabled());
			assertEquals("Re-enabling the same MIDI route must reuse the existing input.",
					1, input.openCount());

			input.send(MockMidiInstrument.shortMessage(javax.sound.midi.ShortMessage.NOTE_ON, 1, 64, 100));
			stdout.awaitContains("MIDI monitor input detected", 2_000);
		}
	}

	@Test
	public void setMidiSettingsReportsOpenFailureForMissingMockInstrument() throws Exception {
		MockMidiInstrument input = new MockMidiInstrument("Available Mock");

		try (AutoCloseable ignored = MockMidiInstrument.install(input);
				ServerHarness server = ServerHarness.start()) {
			server.hello();
			server.send(SrapProtocol.SET_MIDI_SETTINGS, midiSettingsPayload(202, true,
					new MidiAssignment(1, true, 1, "missing-device")));

			SrapProtocol.Frame error = server.readFrame(SrapProtocol.ERROR, 202);
			SrapProtocol.PayloadReader in = SrapProtocol.reader(error.payload());
			assertEquals(202, in.u32());
			assertEquals(SrapProtocol.ERR_PLAYBACK_ERROR, in.u16());
			in.u16();
			assertTrue(in.str().contains("No MIDI input device matches: missing-device"));
		}
	}

	private static byte[] midiSettingsPayload(long requestId, boolean enabled, MidiAssignment... assignments) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u32(requestId)
				.u8(enabled ? 1 : 0)
				.u8(assignments.length)
				.u16(0);
		for (MidiAssignment assignment : assignments) {
			out.u8(assignment.voiceIndex())
					.u8(assignment.voiceEnabled() ? 1 : 0)
					.u8(assignment.channel())
					.u8(0)
					.str(assignment.deviceSelector());
		}
		return out.toByteArray();
	}

	private record MidiAssignment(int voiceIndex, boolean voiceEnabled, int channel, String deviceSelector) {
	}

	private record MidiDeviceEntry(int index, String selector, String displayName, String name,
			String vendor, String description, String version) {
	}

	private record MidiDeviceList(long requestId, List<MidiDeviceEntry> devices) {
		static MidiDeviceList read(SrapProtocol.Frame frame) {
			SrapProtocol.PayloadReader in = SrapProtocol.reader(frame.payload());
			long requestId = in.u32();
			int count = in.u16();
			List<MidiDeviceEntry> devices = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				devices.add(new MidiDeviceEntry(in.u16(), in.str(), in.str(), in.str(), in.str(),
						in.str(), in.str()));
			}
			return new MidiDeviceList(requestId, devices);
		}
	}

	private record MidiStateAssignment(int voiceIndex, boolean voiceEnabled, int channel,
			String deviceSelector, String deviceName) {
	}

	private record MidiState(long requestId, boolean enabled, List<MidiStateAssignment> assignments) {
		static MidiState read(SrapProtocol.Frame frame) {
			SrapProtocol.PayloadReader in = SrapProtocol.reader(frame.payload());
			long requestId = in.u32();
			boolean enabled = in.u8() != 0;
			int count = in.u8();
			in.u16();
			List<MidiStateAssignment> assignments = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				assignments.add(new MidiStateAssignment(in.u8(), in.u8() != 0, in.u8(),
						skipReservedThenReadSelector(in), in.str()));
			}
			return new MidiState(requestId, enabled, assignments);
		}

		MidiStateAssignment assignment(int voiceIndex) {
			return assignments.stream()
					.filter(assignment -> assignment.voiceIndex() == voiceIndex)
					.findFirst()
					.orElseThrow();
		}

		private static String skipReservedThenReadSelector(SrapProtocol.PayloadReader in) {
			in.u8();
			return in.str();
		}
	}

	private static final class CapturedStdout implements AutoCloseable {
		private final PrintStream original;
		private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
		private final PrintStream capture;

		private CapturedStdout(PrintStream original) {
			this.original = original;
			this.capture = new PrintStream(captured, true, StandardCharsets.UTF_8);
			System.setOut(capture);
		}

		static CapturedStdout start() {
			return new CapturedStdout(System.out);
		}

		void awaitContains(String needle, long timeoutMillis) throws InterruptedException {
			long deadline = System.currentTimeMillis() + timeoutMillis;
			while (System.currentTimeMillis() < deadline) {
				if (text().contains(needle)) {
					return;
				}
				Thread.sleep(10);
			}
			throw new AssertionError("Timed out waiting for stdout to contain '" + needle + "'. Captured output:\n"
					+ text());
		}

		private synchronized String text() {
			return captured.toString(StandardCharsets.UTF_8);
		}

		@Override
		public void close() {
			System.setOut(original);
			capture.close();
		}
	}

	private static final class ServerHarness implements AutoCloseable {
		private final Thread serverThread;
		private final Socket socket;
		private final AtomicBoolean closing;
		private final AtomicReference<Exception> serverFailure;
		private long sequence = 1;

		private ServerHarness(Thread serverThread, Socket socket,
				AtomicBoolean closing, AtomicReference<Exception> serverFailure) {
			this.serverThread = serverThread;
			this.socket = socket;
			this.closing = closing;
			this.serverFailure = serverFailure;
		}

		static ServerHarness start() throws Exception {
			int port = freeLoopbackPort();
			SIDScorePlayerServer server = new SIDScorePlayerServer(port);
			AtomicBoolean closing = new AtomicBoolean(false);
			AtomicReference<Exception> serverFailure = new AtomicReference<>();
			Thread serverThread = new Thread(() -> {
				try {
					server.run();
				} catch (Exception e) {
					if (!closing.get() || !isExpectedClose(e)) {
						serverFailure.set(e);
					}
				}
			}, "sidscore-srap-test-server");
			serverThread.start();

			Socket socket = connectWithRetry(port);
			socket.setSoTimeout(2_000);
			return new ServerHarness(serverThread, socket, closing, serverFailure);
		}

		void hello() throws IOException {
			send(SrapProtocol.HELLO, SrapProtocol.payload()
					.str("sidscore-test")
					.u16(SrapProtocol.VERSION)
					.u16(SrapProtocol.VERSION)
					.u32(SrapProtocol.CAP_ALL)
					.toByteArray());
			readFrame(SrapProtocol.HELLO_ACK, -1);
		}

		void send(int type, byte[] payload) throws IOException {
			SrapProtocol.writeFrame(socket.getOutputStream(), type, 0, sequence++, payload);
		}

		SrapProtocol.Frame readFrame(int expectedType, long expectedRequestId) throws IOException {
			long deadline = System.currentTimeMillis() + 2_000;
			while (System.currentTimeMillis() < deadline) {
				SrapProtocol.Frame frame = SrapProtocol.readFrame(socket.getInputStream());
				if (frame.type() != expectedType) {
					continue;
				}
				if (expectedRequestId >= 0 && requestId(frame) != expectedRequestId) {
					continue;
				}
				return frame;
			}
			throw new SocketTimeoutException("Timed out waiting for SRAP frame type " + expectedType
					+ " requestId " + expectedRequestId);
		}

		void waitUntil(BooleanCondition condition) throws Exception {
			long deadline = System.currentTimeMillis() + 2_000;
			while (System.currentTimeMillis() < deadline) {
				if (condition.getAsBoolean()) {
					return;
				}
				Thread.sleep(10);
			}
			throw new AssertionError("Timed out waiting for mocked MIDI instrument condition");
		}

		@Override
		public void close() throws Exception {
			closing.set(true);
			socket.close();
			serverThread.join(2_000);
			if (serverThread.isAlive()) {
				throw new AssertionError("SRAP test server did not stop");
			}
			Exception failure = serverFailure.get();
			if (failure != null) {
				throw new AssertionError("SRAP test server failed", failure);
			}
		}

		private static long requestId(SrapProtocol.Frame frame) {
			if (frame.payload().length < 4) {
				return -1;
			}
			return SrapProtocol.reader(frame.payload()).u32();
		}

		private static int freeLoopbackPort() throws IOException {
			try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
				return serverSocket.getLocalPort();
			}
		}

		private static Socket connectWithRetry(int port) throws Exception {
			long deadline = System.currentTimeMillis() + 2_000;
			IOException last = null;
			while (System.currentTimeMillis() < deadline) {
				try {
					return new Socket(InetAddress.getByName("127.0.0.1"), port);
				} catch (IOException e) {
					last = e;
					Thread.sleep(10);
				}
			}
			throw last != null ? last : new EOFException("Could not connect to SRAP test server");
		}

		private static boolean isExpectedClose(Exception e) {
			return e instanceof EOFException || e instanceof SocketException;
		}
	}

	@FunctionalInterface
	private interface BooleanCondition {
		boolean getAsBoolean();
	}
}
