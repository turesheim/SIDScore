/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Torkild Ulvøy Resheim <torkildr@gmail.com> - initial API and implementation
 */
package net.resheim.sidscore.sid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SidWaveforms {
	public static final int WAVE_LEN = 4096;
	public static final int PULSE_WAVE_LEN = 8192;
	// Clean-room combined waveform generator with optional external table overrides.

	public static final class TableSet {
		public final byte[] wave30;
		public final byte[] wave50;
		public final byte[] wave60;
		public final byte[] wave70;

		TableSet(byte[] wave30, byte[] wave50, byte[] wave60, byte[] wave70) {
			this.wave30 = wave30;
			this.wave50 = wave50;
			this.wave60 = wave60;
			this.wave70 = wave70;
		}
	}

	public static TableSet loadTables(SidModel model, Path externalPath) {
		TableSet generated = generate(model);
		if (externalPath == null) {
			Path defaultDir = Path.of("waveforms");
			if (Files.isDirectory(defaultDir)) {
				externalPath = defaultDir;
			} else {
				return generated;
			}
		}
		try {
			ExternalTables ext = loadExternal(externalPath);
			if (ext == null) {
				return generated;
			}
			TableSet override = ext.forModel(model);
			return merge(generated, override);
		} catch (IOException e) {
			System.err.println("SID waveforms: failed to load external tables from " + externalPath + ": "
					+ e.getMessage());
			return generated;
		}
	}

	private static TableSet merge(TableSet base, TableSet override) {
		if (override == null) {
			return base;
		}
		return new TableSet(
				override.wave30 != null ? override.wave30 : base.wave30,
				override.wave50 != null ? override.wave50 : base.wave50,
				override.wave60 != null ? override.wave60 : base.wave60,
				override.wave70 != null ? override.wave70 : base.wave70);
	}

	private static ExternalTables loadExternal(Path path) throws IOException {
		if (path == null) {
			return null;
		}
		if (Files.isDirectory(path)) {
			return ExternalTables.fromDirectory(path);
		}
		if (!Files.exists(path)) {
			return null;
		}
		String text = Files.readString(path);
		return ExternalTables.fromHeader(text);
	}

	private static TableSet generate(SidModel model) {
		byte[] wave30 = new byte[WAVE_LEN];
		byte[] wave50 = new byte[PULSE_WAVE_LEN];
		byte[] wave60 = new byte[PULSE_WAVE_LEN];
		byte[] wave70 = new byte[PULSE_WAVE_LEN];

		for (int i = 0; i < WAVE_LEN; i++) {
			int tri = triangle12(i);
			int saw = i & 0x0FFF;
			int combined = combine(tri, saw, 0x0FFF, true, true, false);
			wave30[i] = (byte) shapeTo8(combined, model);
		}

		for (int i = 0; i < PULSE_WAVE_LEN; i++) {
			int phase = i & 0x0FFF;
			int tri = triangle12(phase);
			int saw = phase;
			int pulse = (i < 4096) ? 0x0FFF : 0x0000;

			int triPulse = combine(tri, saw, pulse, true, false, true);
			int sawPulse = combine(tri, saw, pulse, false, true, true);
			int triSawPulse = combine(tri, saw, pulse, true, true, true);

			wave50[i] = (byte) shapeTo8(triPulse, model);
			wave60[i] = (byte) shapeTo8(sawPulse, model);
			wave70[i] = (byte) shapeTo8(triSawPulse, model);
		}

		if (model == SidModel.MOS6581) {
			smoothInPlace(wave30, 0.35);
			smoothInPlace(wave50, 0.35);
			smoothInPlace(wave60, 0.35);
			smoothInPlace(wave70, 0.35);
		}

		return new TableSet(wave30, wave50, wave60, wave70);
	}

	private static int triangle12(int phase) {
		int p = phase & 0x0FFF;
		return (p < 2048) ? (p << 1) : ((0x0FFF - p) << 1);
	}

	private static int combine(int tri, int saw, int pulse, boolean useTri, boolean useSaw, boolean usePulse) {
		int v = 0x0FFF;
		if (useTri) {
			v &= tri;
		}
		if (useSaw) {
			v &= saw;
		}
		if (usePulse) {
			v &= pulse;
		}
		return v;
	}

	private static int shapeTo8(int combined12, SidModel model) {
		double v = Math.max(0.0, Math.min(1.0, combined12 / 4095.0));
		if (model == SidModel.MOS6581) {
			v = Math.pow(v, 1.25);
			v = Math.min(1.0, v + 0.015);
		}
		int out = (int) Math.round(v * 255.0);
		if (out < 0) {
			out = 0;
		} else if (out > 255) {
			out = 255;
		}
		return out;
	}

	private static void smoothInPlace(byte[] data, double alpha) {
		if (data.length == 0) {
			return;
		}
		double prev = data[data.length - 1] & 0xFF;
		for (int i = 0; i < data.length; i++) {
			double cur = data[i] & 0xFF;
			prev = prev + alpha * (cur - prev);
			int v = (int) Math.round(prev);
			if (v < 0) {
				v = 0;
			} else if (v > 255) {
				v = 255;
			}
			data[i] = (byte) v;
		}
	}

	private static final class ExternalTables {
		private final TableSet mos6581;
		private final TableSet mos8580;

		private ExternalTables(TableSet mos6581, TableSet mos8580) {
			this.mos6581 = mos6581;
			this.mos8580 = mos8580;
		}

		TableSet forModel(SidModel model) {
			return model == SidModel.MOS6581 ? mos6581 : mos8580;
		}

		static ExternalTables fromDirectory(Path dir) throws IOException {
			TableSet t6581 = loadFromDir(dir, "6581");
			TableSet t8580 = loadFromDir(dir, "8580");
			if (t6581 == null && t8580 == null) {
				return null;
			}
			return new ExternalTables(t6581, t8580);
		}

		static ExternalTables fromHeader(String text) {
			Map<String, byte[]> map = parseArrays(text);
			TableSet t6581 = fromMap(map, "6581");
			TableSet t8580 = fromMap(map, "8580");
			if (t6581 == null && t8580 == null) {
				return null;
			}
			return new ExternalTables(t6581, t8580);
		}

		private static TableSet loadFromDir(Path dir, String suffix) throws IOException {
			byte[] w30 = readBin(dir.resolve("waveform30_" + suffix + ".bin"), WAVE_LEN);
			byte[] w50 = readBin(dir.resolve("waveform50_" + suffix + ".bin"), PULSE_WAVE_LEN);
			byte[] w60 = readBin(dir.resolve("waveform60_" + suffix + ".bin"), PULSE_WAVE_LEN);
			byte[] w70 = readBin(dir.resolve("waveform70_" + suffix + ".bin"), PULSE_WAVE_LEN);
			if (w30 == null && w50 == null && w60 == null && w70 == null) {
				return null;
			}
			return new TableSet(w30, w50, w60, w70);
		}

		private static byte[] readBin(Path path, int expectedLen) throws IOException {
			if (!Files.exists(path)) {
				return null;
			}
			byte[] data = Files.readAllBytes(path);
			if (data.length != expectedLen) {
				return null;
			}
			return data;
		}

		private static Map<String, byte[]> parseArrays(String text) {
			Map<String, byte[]> out = new HashMap<>();
			for (String name : new String[] {
					"waveform30_6581", "waveform50_6581", "waveform60_6581", "waveform70_6581",
					"waveform30_8580", "waveform50_8580", "waveform60_8580", "waveform70_8580"
			}) {
				byte[] arr = parseArray(text, name);
				if (arr != null) {
					out.put(name.toLowerCase(Locale.ROOT), arr);
				}
			}
			return out;
		}

		private static byte[] parseArray(String text, String name) {
			Pattern p = Pattern.compile(name + "\\s*\\[[^\\]]*\\]\\s*=\\s*\\{(.*?)\\}",
					Pattern.DOTALL);
			Matcher m = p.matcher(text);
			if (!m.find()) {
				return null;
			}
			String body = m.group(1);
			Matcher nums = Pattern.compile("0x[0-9a-fA-F]+|\\d+").matcher(body);
			int expected = name.contains("30") ? WAVE_LEN : PULSE_WAVE_LEN;
			byte[] data = new byte[expected];
			int idx = 0;
			while (nums.find() && idx < expected) {
				String token = nums.group();
				int v = token.startsWith("0x") || token.startsWith("0X")
						? Integer.parseInt(token.substring(2), 16)
						: Integer.parseInt(token);
				data[idx++] = (byte) (v & 0xFF);
			}
			if (idx != expected) {
				return null;
			}
			return data;
		}

		private static TableSet fromMap(Map<String, byte[]> map, String suffix) {
			String key = suffix.toLowerCase(Locale.ROOT);
			byte[] w30 = map.get(("waveform30_" + key));
			byte[] w50 = map.get(("waveform50_" + key));
			byte[] w60 = map.get(("waveform60_" + key));
			byte[] w70 = map.get(("waveform70_" + key));
			if (w30 == null && w50 == null && w60 == null && w70 == null) {
				return null;
			}
			return new TableSet(w30, w50, w60, w70);
		}
	}

	private SidWaveforms() {
	}
}
