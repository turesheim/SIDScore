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

public enum SidModel {
	MOS6581,
	MOS8580;

	public static SidModel parse(String raw) {
		if (raw == null) {
			return MOS6581;
		}
		String v = raw.trim().toLowerCase();
		return switch (v) {
		case "8580", "mos8580" -> MOS8580;
		case "6581", "mos6581" -> MOS6581;
		default -> throw new IllegalArgumentException("Unknown SID model: " + raw);
		};
	}

	public int psidFlagBits() {
		return this == MOS6581 ? 0x10 : 0x20;
	}
}
