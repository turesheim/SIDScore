/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore;

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

	@Test
	public void exportSourceWritesAsmPrgAndWavOverSrap() throws Exception {
		Path workDir = Files.createTempDirectory("sidscore-srap-export-test-");
		try {
			Path sourcePath = workDir.resolve("export-test.sidscore");
			Files.writeString(sourcePath, SOURCE);

			try (ServerHarness server = ServerHarness.start()) {
				server.hello();
				assertExport(server, sourcePath, sourcePath.toUri().toString(), workDir.resolve("export-test.asm"), 1);
				assertExport(server, sourcePath, sourcePath.toUri().toString(), workDir.resolve("export-test.prg"), 2);
				Path wavPath = workDir.resolve("export-test.wav");
				assertExport(server, sourcePath, sourcePath.toUri().toString(), wavPath, 3);
				assertArrayEquals(new byte[] { 'R', 'I', 'F', 'F' },
						java.util.Arrays.copyOf(Files.readAllBytes(wavPath), 4));
			}
		} finally {
			deleteRecursively(workDir);
		}
	}

	private static void assertExport(ServerHarness server, Path sourcePath, String sourceUri, Path outputPath,
			int format) throws Exception {
		long requestId = 300 + format;
		byte[] sourceBytes = SOURCE.getBytes(StandardCharsets.UTF_8);
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
