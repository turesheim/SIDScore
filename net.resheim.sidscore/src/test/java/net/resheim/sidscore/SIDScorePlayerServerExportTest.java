/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore;

import net.resheim.sidscore.ir.SIDScoreIR;
import net.resheim.sidscore.server.SrapProtocol;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SIDScorePlayerServerExportTest {

	private static final String SOURCE = """
			TITLE "Export Test"
			AUTHOR "SIDScore"
			TEMPO 120
			SYSTEM PAL

			INSTR lead WAVE=PULSE ADSR=0,4,10,4 PW=$0800

			VOICE 1 lead:
			  O4 L8 C D
			""";

	private static final String SOURCE_WITH_VIBRATO = """
			TITLE "Vibrato Export Test"
			AUTHOR "SIDScore"
			TEMPO 120
			SYSTEM PAL

			INSTR lead WAVE=PULSE ADSR=0,4,10,4 PW=$0800 VIBRATO=4,16,64,0

			VOICE 1 lead:
			  O4 L8 C D
			""";

	@Test
	public void exportSourceWritesAsmPrgSidAndWavOverSrap() throws Exception {
		Path workDir = Files.createTempDirectory("sidscore-srap-export-test-");
		try {
			Path sourcePath = workDir.resolve("export-test.sidscore");
			Files.writeString(sourcePath, SOURCE);

			try (ServerHarness server = ServerHarness.start()) {
				server.hello();
				assertExport(server, sourcePath, sourcePath.toUri().toString(), workDir.resolve("export-test.asm"), 1);
				assertExport(server, sourcePath, sourcePath.toUri().toString(), workDir.resolve("export-test.prg"), 2);
				Path sidPath = workDir.resolve("export-test.sid");
				assertExport(server, sourcePath, sourcePath.toUri().toString(), sidPath, 4);
				assertArrayEquals(new byte[] { 'P', 'S', 'I', 'D' },
						java.util.Arrays.copyOf(Files.readAllBytes(sidPath), 4));
				Path wavPath = workDir.resolve("export-test.wav");
				assertExport(server, sourcePath, sourcePath.toUri().toString(), wavPath, 3);
				assertArrayEquals(new byte[] { 'R', 'I', 'F', 'F' },
						java.util.Arrays.copyOf(Files.readAllBytes(wavPath), 4));
			}
		} finally {
			deleteRecursively(workDir);
		}
	}


	@Test
	public void setInstrumentRoundTripsOptionalVibratoOverSrap() throws Exception {
		try (ServerHarness server = ServerHarness.start()) {
			server.hello();
			server.send(SrapProtocol.SET_INSTRUMENT, setInstrumentPayload(310, true));

			InstrumentState state = InstrumentState.read(server.readFrame(SrapProtocol.INSTRUMENT_STATE, 310));
			assertEquals(3, state.voiceIndex());
			assertEquals(2, state.source());
			assertEquals("vib", state.name());
			assertEquals(12, state.vibratoDelay());
			assertEquals(24, state.vibratoRate());
			assertEquals(36, state.vibratoAmp());
			assertEquals(48, state.vibratoInc());

			server.send(SrapProtocol.SET_INSTRUMENT, setInstrumentPayload(311, false));
			InstrumentState legacyState = InstrumentState.read(server.readFrame(SrapProtocol.INSTRUMENT_STATE, 311));
			assertEquals("vib", legacyState.name());
			assertEquals(0, legacyState.vibratoDelay());
			assertEquals(0, legacyState.vibratoRate());
			assertEquals(0, legacyState.vibratoAmp());
			assertEquals(0, legacyState.vibratoInc());
		}
	}

	@Test
	public void exportSourceWritesGeneratedVibseqForInstrumentOverride() throws Exception {
		Path workDir = Files.createTempDirectory("sidscore-srap-vibseq-export-test-");
		try {
			Path sourcePath = workDir.resolve("vibseq-export-test.sidscore");
			Files.writeString(sourcePath, SOURCE);

			try (ServerHarness server = ServerHarness.start()) {
				server.hello();
				server.send(SrapProtocol.SET_INSTRUMENT, setInstrumentPayload(312, 1, true));
				InstrumentState.read(server.readFrame(SrapProtocol.INSTRUMENT_STATE, 312));

				Path asmPath = workDir.resolve("vibseq-export-test.asm");
				assertExport(server, sourcePath, sourcePath.toUri().toString(), asmPath, 1);
				String asm = Files.readString(asmPath);
				assertTrue(asm.contains("jsr v1_vibseq_update"));
				assertTrue(asm.contains("v1_vibseq_reset:"));
				assertTrue(asm.contains("adc v1_vib_off"));
				assertTrue(asm.contains("// VIBSEQ generated fine pitch tables (signed SID frequency deltas)"));
				assertTrue(asm.contains("vib_table_0_loop:"));
			}
		} finally {
			deleteRecursively(workDir);
		}
	}

	@Test
	public void exportSourceWritesGeneratedVibseqForSourceVibrato() throws Exception {
		Path workDir = Files.createTempDirectory("sidscore-source-vibseq-export-test-");
		try {
			Path sourcePath = workDir.resolve("source-vibseq-export-test.sidscore");
			Files.writeString(sourcePath, SOURCE_WITH_VIBRATO);

			try (ServerHarness server = ServerHarness.start()) {
				server.hello();

				Path asmPath = workDir.resolve("source-vibseq-export-test.asm");
				assertExport(server, sourcePath, sourcePath.toUri().toString(), asmPath, 1, SOURCE_WITH_VIBRATO);
				String asm = Files.readString(asmPath);
				assertTrue(asm.contains("jsr v1_vibseq_update"));
				assertTrue(asm.contains("// vib_table_0: ref MIDI 60 delay 4 rate 16 amp 64 inc 0"));
				assertTrue(asm.contains("vib_table_0_loop:"));
			}
		} finally {
			deleteRecursively(workDir);
		}
	}

	private static void assertExport(ServerHarness server, Path sourcePath, String sourceUri, Path outputPath,
			int format) throws Exception {
		assertExport(server, sourcePath, sourceUri, outputPath, format, SOURCE);
	}

	private static void assertExport(ServerHarness server, Path sourcePath, String sourceUri, Path outputPath,
			int format, String sourceText) throws Exception {
		long requestId = 300 + format;
		byte[] sourceBytes = sourceText.getBytes(StandardCharsets.UTF_8);
		server.send(SrapProtocol.EXPORT_SOURCE, SrapProtocol.payload()
				.u32(requestId)
				.str(sourceUri)
				.str(sourcePath.toString())
				.u8(0)
				.u8(format)
				.u8(0)
				.u8(0)
				.str(outputPath.toString())
				.u32(sourceBytes.length)
				.bytes(sourceBytes)
				.u16(1)
				.toByteArray());

		SrapProtocol.PayloadReader result = SrapProtocol.reader(
				server.readFrame(SrapProtocol.EXPORT_RESULT, requestId).payload());
		assertEquals(requestId, result.u32());
		assertEquals(format, result.u8());
		result.u8();
		result.u8();
		result.u8();
		assertEquals(outputPath.toString(), result.str());
		assertTrue("Exported file should not be empty: " + outputPath, result.u64() > 0);
		assertTrue(Files.isRegularFile(outputPath));
		assertTrue("Exported file should have bytes on disk: " + outputPath, Files.size(outputPath) > 0);
	}


	private static byte[] setInstrumentPayload(long requestId, boolean includeVibrato) {
		return setInstrumentPayload(requestId, 3, includeVibrato);
	}

	private static byte[] setInstrumentPayload(long requestId, int voiceIndex, boolean includeVibrato) {
		SrapProtocol.PayloadWriter out = SrapProtocol.payload()
				.u32(requestId)
				.u8(voiceIndex)
				.u8(SIDScoreIR.Wave.PULSE.mask)
				.u8(1)
				.u8(2)
				.u8(3)
				.u8(4)
				.u16(0x0900)
				.i16(-7)
				.u16(0x0100)
				.u16(0x0F00)
				.u8(0)
				.u16(0)
				.u8(0)
				.u8(0)
				.u8(0)
				.u8(0)
				.u8(0)
				.str("vib");
		if (includeVibrato) {
			out.u8(12)
					.u8(24)
					.u8(36)
					.u8(48);
		}
		return out.toByteArray();
	}

	private record InstrumentState(int voiceIndex, int source, String name,
			int vibratoDelay, int vibratoRate, int vibratoAmp, int vibratoInc) {
		static InstrumentState read(SrapProtocol.Frame frame) {
			SrapProtocol.PayloadReader in = SrapProtocol.reader(frame.payload());
			in.u32();
			int voiceIndex = in.u8();
			int source = in.u8();
			in.u16();
			in.u8();
			in.u8();
			in.u8();
			in.u8();
			in.u8();
			in.u16();
			in.i16();
			in.u16();
			in.u16();
			in.u8();
			in.u16();
			in.u8();
			in.u8();
			in.u8();
			in.u8();
			in.u8();
			String name = in.str();
			int vibratoDelay = 0;
			int vibratoRate = 0;
			int vibratoAmp = 0;
			int vibratoInc = 0;
			if (in.remaining() >= 4) {
				vibratoDelay = in.u8();
				vibratoRate = in.u8();
				vibratoAmp = in.u8();
				vibratoInc = in.u8();
			}
			return new InstrumentState(voiceIndex, source, name,
					vibratoDelay, vibratoRate, vibratoAmp, vibratoInc);
		}
	}

	private static void deleteRecursively(Path path) throws IOException {
		if (path == null || !Files.exists(path)) {
			return;
		}
		try (var stream = Files.walk(path)) {
			for (Path item : stream
					.sorted((left, right) -> Integer.compare(right.getNameCount(), left.getNameCount()))
					.toList()) {
				Files.deleteIfExists(item);
			}
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
			}, "sidscore-srap-export-test-server");
			serverThread.start();

			Socket socket = connectWithRetry(port);
			socket.setSoTimeout(5_000);
			return new ServerHarness(serverThread, socket, closing, serverFailure);
		}

		void hello() throws IOException {
			send(SrapProtocol.HELLO, SrapProtocol.payload()
					.str("sidscore-export-test")
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
			long deadline = System.currentTimeMillis() + 5_000;
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
}
