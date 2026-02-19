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

import net.resheim.sidscore.export.SIDScoreExporter;
import net.resheim.sidscore.ir.SIDScoreIR;

/**
 * Built-in SIDScore driver backend.
 *
 * This backend is intended to reproduce SRAP behavior as closely as possible in
 * exported ASM/PRG/SID output.
 */
public final class SidScoreDriverBackend implements SidDriverBackend {

	private final SIDScoreExporter exporter = new SIDScoreExporter();

	@Override
	public String id() {
		return "sidscore";
	}

	@Override
	public String description() {
		return "SIDScore reference driver (SRAP-aligned)";
	}

	@Override
	public void writeAsm(SIDScoreIR.TimedScore score, Path outAsm, boolean installIrq) throws IOException {
		exporter.writeAsm(score, outAsm, installIrq);
	}

	@Override
	public DriverAddresses psidAddresses() {
		return new DriverAddresses(
				SIDScoreExporter.BASIC_LOAD_ADDR,
				SIDScoreExporter.LOAD_ADDR,
				SIDScoreExporter.PLAY_ADDR
		);
	}
}
