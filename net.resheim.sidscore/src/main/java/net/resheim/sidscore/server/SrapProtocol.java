/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class SrapProtocol {
	public static final int MAGIC = 0x53524150; // "SRAP"
	public static final int VERSION = 1;
	public static final int HEADER_BYTES = 24;
	public static final int MAX_PAYLOAD_BYTES = 4 * 1024 * 1024;

	public static final int HELLO = 0x01;
	public static final int HELLO_ACK = 0x02;

	public static final int PLAY = 0x10;
	public static final int PAUSE = 0x11;
	public static final int CONTINUE = 0x12;
	public static final int STOP = 0x13;

	public static final int PLAYBACK_STATE = 0x20;
	public static final int SCORE_MAP = 0x21;
	public static final int HIGHLIGHT_STATE = 0x22;
	public static final int VOICE_STATE = 0x23;
	public static final int SCOPE_BUCKETS = 0x24;

	public static final int ERROR = 0x7f;

	public static final int CAP_SCORE_MAP = 1;
	public static final int CAP_HIGHLIGHT_STATE = 1 << 1;
	public static final int CAP_VOICE_STATE = 1 << 2;
	public static final int CAP_SCOPE_BUCKETS = 1 << 3;
	public static final int CAP_ALL = CAP_SCORE_MAP | CAP_HIGHLIGHT_STATE | CAP_VOICE_STATE | CAP_SCOPE_BUCKETS;

	public static final int STATE_IDLE = 0;
	public static final int STATE_LOADING = 1;
	public static final int STATE_PLAYING = 2;
	public static final int STATE_PAUSED = 3;
	public static final int STATE_STOPPED = 4;
	public static final int STATE_ENDED = 5;
	public static final int STATE_ERROR = 6;

	public static final int REASON_NONE = 0;
	public static final int REASON_CLIENT_REQUEST = 1;
	public static final int REASON_END_OF_SCORE = 2;
	public static final int REASON_PARSE_ERROR = 3;
	public static final int REASON_PLAYBACK_ERROR = 4;
	public static final int REASON_CONNECTION_CLOSED = 5;

	public static final int ERR_UNSUPPORTED_VERSION = 1;
	public static final int ERR_INVALID_FRAME = 2;
	public static final int ERR_INVALID_STATE = 3;
	public static final int ERR_FILE_NOT_FOUND = 4;
	public static final int ERR_PARSE_ERROR = 5;
	public static final int ERR_RESOLVE_ERROR = 6;
	public static final int ERR_PLAYBACK_ERROR = 7;
	public static final int ERR_INTERNAL_ERROR = 8;

	private SrapProtocol() {
	}

	public static Frame readFrame(InputStream in) throws IOException {
		byte[] header = in.readNBytes(HEADER_BYTES);
		if (header.length == 0) {
			throw new EOFException();
		}
		if (header.length != HEADER_BYTES) {
			throw new EOFException("Short SRAP frame header");
		}
		ByteBuffer h = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
		int magic = h.getInt();
		if (magic != MAGIC) {
			throw new IOException("Invalid SRAP magic: 0x" + Integer.toHexString(magic));
		}
		int version = h.get() & 0xFF;
		if (version != VERSION) {
			throw new IOException("Unsupported SRAP version: " + version);
		}
		int type = h.get() & 0xFF;
		int flags = h.getShort() & 0xFFFF;
		long sequence = Integer.toUnsignedLong(h.getInt());
		long timestampNanos = h.getLong();
		int payloadLength = h.getInt();
		if (payloadLength < 0 || payloadLength > MAX_PAYLOAD_BYTES) {
			throw new IOException("Invalid SRAP payload length: " + payloadLength);
		}
		byte[] payload = in.readNBytes(payloadLength);
		if (payload.length != payloadLength) {
			throw new EOFException("Short SRAP frame payload");
		}
		return new Frame(type, flags, sequence, timestampNanos, payload);
	}

	public static void writeFrame(OutputStream out, int type, int flags, long sequence, byte[] payload)
			throws IOException {
		byte[] body = payload != null ? payload : new byte[0];
		if (body.length > MAX_PAYLOAD_BYTES) {
			throw new IOException("SRAP payload too large: " + body.length);
		}
		ByteBuffer header = ByteBuffer.allocate(HEADER_BYTES).order(ByteOrder.LITTLE_ENDIAN);
		header.putInt(MAGIC);
		header.put((byte) VERSION);
		header.put((byte) type);
		header.putShort((short) flags);
		header.putInt((int) sequence);
		header.putLong(System.nanoTime());
		header.putInt(body.length);
		out.write(header.array());
		out.write(body);
		out.flush();
	}

	public static PayloadWriter payload() {
		return new PayloadWriter();
	}

	public static PayloadReader reader(byte[] payload) {
		return new PayloadReader(payload);
	}

	public static final record Frame(int type, int flags, long sequence, long timestampNanos, byte[] payload) {
	}

	public static final class PayloadWriter {
		private byte[] data = new byte[256];
		private int pos = 0;

		public PayloadWriter u8(int value) {
			ensure(1);
			data[pos++] = (byte) value;
			return this;
		}

		public PayloadWriter i8(int value) {
			return u8(value);
		}

		public PayloadWriter u16(int value) {
			ensure(2);
			data[pos++] = (byte) value;
			data[pos++] = (byte) (value >>> 8);
			return this;
		}

		public PayloadWriter i16(int value) {
			return u16(value);
		}

		public PayloadWriter u32(long value) {
			ensure(4);
			data[pos++] = (byte) value;
			data[pos++] = (byte) (value >>> 8);
			data[pos++] = (byte) (value >>> 16);
			data[pos++] = (byte) (value >>> 24);
			return this;
		}

		public PayloadWriter i32(int value) {
			return u32(value);
		}

		public PayloadWriter u64(long value) {
			ensure(8);
			data[pos++] = (byte) value;
			data[pos++] = (byte) (value >>> 8);
			data[pos++] = (byte) (value >>> 16);
			data[pos++] = (byte) (value >>> 24);
			data[pos++] = (byte) (value >>> 32);
			data[pos++] = (byte) (value >>> 40);
			data[pos++] = (byte) (value >>> 48);
			data[pos++] = (byte) (value >>> 56);
			return this;
		}

		public PayloadWriter f32(float value) {
			return u32(Float.floatToIntBits(value));
		}

		public PayloadWriter str(String value) {
			byte[] bytes = (value != null ? value : "").getBytes(StandardCharsets.UTF_8);
			if (bytes.length > 0xFFFF) {
				throw new IllegalArgumentException("String too long for SRAP payload");
			}
			u16(bytes.length);
			ensure(bytes.length);
			System.arraycopy(bytes, 0, data, pos, bytes.length);
			pos += bytes.length;
			return this;
		}

		public PayloadWriter bytes(byte[] value) {
			byte[] bytes = value != null ? value : new byte[0];
			ensure(bytes.length);
			System.arraycopy(bytes, 0, data, pos, bytes.length);
			pos += bytes.length;
			return this;
		}

		public byte[] toByteArray() {
			return Arrays.copyOf(data, pos);
		}

		private void ensure(int count) {
			int need = pos + count;
			if (need <= data.length) {
				return;
			}
			int newLen = data.length;
			while (newLen < need) {
				newLen *= 2;
			}
			data = Arrays.copyOf(data, newLen);
		}
	}

	public static final class PayloadReader {
		private final ByteBuffer data;

		private PayloadReader(byte[] payload) {
			this.data = ByteBuffer.wrap(payload != null ? payload : new byte[0]).order(ByteOrder.LITTLE_ENDIAN);
		}

		public int u8() {
			return data.get() & 0xFF;
		}

		public int u16() {
			return data.getShort() & 0xFFFF;
		}

		public long u32() {
			return Integer.toUnsignedLong(data.getInt());
		}

		public int i32() {
			return data.getInt();
		}

		public long u64() {
			return data.getLong();
		}

		public String str() {
			int len = u16();
			byte[] bytes = new byte[len];
			data.get(bytes);
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}
}
