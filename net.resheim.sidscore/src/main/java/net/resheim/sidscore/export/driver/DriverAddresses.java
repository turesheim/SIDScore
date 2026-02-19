/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.export.driver;

/**
 * Memory addresses used by a SID driver build for PSID metadata.
 *
 * @param loadAddress the C64 load address for the PRG image
 * @param initAddress the init routine address called by SID players
 * @param playAddress the play routine address called at 50/60 Hz
 */
public record DriverAddresses(int loadAddress, int initAddress, int playAddress) {

	public DriverAddresses {
		validate16Bit(loadAddress, "loadAddress");
		validate16Bit(initAddress, "initAddress");
		validate16Bit(playAddress, "playAddress");
	}

	private static void validate16Bit(int value, String name) {
		if (value < 0 || value > 0xFFFF) {
			throw new IllegalArgumentException(name + " must be a 16-bit value, got " + value);
		}
	}
}
