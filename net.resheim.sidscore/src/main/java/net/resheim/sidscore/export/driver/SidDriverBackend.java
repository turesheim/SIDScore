/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.export.driver;

import java.io.IOException;
import java.nio.file.Path;

import net.resheim.sidscore.ir.SIDScoreIR;

/**
 * Backend for emitting assembly driver code from a resolved SIDScore.
 *
 * Implementations may be provided by external plugins using Java
 * {@link java.util.ServiceLoader}.
 */
public interface SidDriverBackend {

	/**
	 * Stable CLI identifier, for example {@code sidscore}.
	 */
	String id();

	/**
	 * Short, user-facing backend description.
	 */
	String description();

	/**
	 * Writes assembler source for the score.
	 *
	 * @param installIrq true for standalone PRG style output; false for PSID output
	 */
	void writeAsm(SIDScoreIR.TimedScore score, Path outAsm, boolean installIrq) throws IOException;

	/**
	 * Address metadata used when wrapping assembled PRG as PSID.
	 */
	DriverAddresses psidAddresses();

	/**
	 * Whether this backend can be wrapped into PSID output.
	 */
	default boolean supportsSidExport() {
		return true;
	}
}
