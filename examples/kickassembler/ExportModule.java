/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * SPDX-License-Identifier: EPL-2.0
 */
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.resheim.sidscore.export.SIDScoreModuleExporter;

/** Generate a module for import into a host KickAssembler program. */
public class ExportModule {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new IllegalArgumentException("Usage: ExportModule.java <source.sidscore> <output.asm>");
		}
		String module = new SIDScoreModuleExporter().generate(Path.of(args[0]), "Score");
		Files.writeString(Path.of(args[1]), module, StandardCharsets.US_ASCII);
	}
}
