/**
 * Copyright (c) 2026 Torkild Ulvøy Resheim.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package net.resheim.sidscore.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.resheim.sidscore.ir.SIDScoreIR;
import net.resheim.sidscore.ir.ScoreBuildingListener;
import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;

/**
 * Source-level entry point for embedding a SIDScore player in a KickAssembler
 * program. No intermediate ASM file or nested KickAssembler invocation is needed.
 */
public final class SIDScoreModuleExporter {
	private final SIDScoreExporter exporter = new SIDScoreExporter();

	/**
	 * Generates a relocatable module at the caller's current program counter.
	 * {@code namespace.init} initializes the SID and player; when multiple tunes
	 * are declared, A selects a one-based tune number (invalid values select 1).
	 * Call {@code namespace.play} once per video frame. Initialization starts
	 * effects declared by the selected tune; {@code namespace.tuneN.effect_Name}
	 * triggers any effect on demand, including one from an unselected tune.
	 * The host owns IRQ installation and must reserve zero-page $fb-$fc while
	 * the player executes.
	 *
	 * @throws IOException if the score cannot be read
	 * @throws IllegalArgumentException if the score or namespace is invalid
	 */
	public String generate(Path sourcePath, String namespace) throws IOException {
		if (sourcePath == null) {
			throw new IllegalArgumentException("SIDScore source path is required");
		}
		Path source = sourcePath.toAbsolutePath().normalize();
		String text = Files.readString(source);
		try {
			SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(text));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SIDScoreParser parser = new SIDScoreParser(tokens);
			BaseErrorListener errors = new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String message, RecognitionException cause) {
					throw new IllegalArgumentException(source + ":" + line + ":" + charPositionInLine
							+ ": " + message, cause);
				}
			};
			lexer.removeErrorListeners();
			lexer.addErrorListener(errors);
			parser.removeErrorListeners();
			parser.addErrorListener(errors);
			ParseTree tree = parser.file();
			ScoreBuildingListener builder = new ScoreBuildingListener(source);
			ParseTreeWalker.DEFAULT.walk(builder, tree);
			SIDScoreIR.ScoreIR ir = builder.buildScoreIR();
			List<SIDScoreIR.TimedScore> tunes = resolveTunes(ir);
			try {
				return exporter.toModuleAsm(tunes, namespace);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(source + ": " + e.getMessage(), e);
			}
		} catch (IllegalStateException | ScoreBuildingListener.ValidationException e) {
			throw new IllegalArgumentException(source + ": " + e.getMessage(), e);
		}
	}

	private List<SIDScoreIR.TimedScore> resolveTunes(SIDScoreIR.ScoreIR score) throws IOException {
		List<SIDScoreIR.TimedScore> tunes = new ArrayList<>();
		tunes.add(new SIDScoreIR.Resolver().resolve(score).timedScore());
		Map<Integer, SIDScoreIR.TimedScore> additional = new TreeMap<>();
		for (var entry : score.songs().entrySet()) {
			additional.put(entry.getKey(), new SIDScoreIR.Resolver().resolve(inlineSongScore(score, entry.getValue()))
					.timedScore());
		}
		for (var entry : score.subtunes().entrySet()) {
			if (additional.containsKey(entry.getKey())) {
				throw new IllegalArgumentException("Duplicate TUNE/IMPORT number " + entry.getKey());
			}
			Path imported = entry.getValue().toAbsolutePath().normalize();
			additional.put(entry.getKey(), resolveImportedTune(imported));
		}
		for (int number = 2; number <= additional.size() + 1; number++) {
			SIDScoreIR.TimedScore tune = additional.get(number);
			if (tune == null) {
				throw new IllegalArgumentException("Subtune numbers must be contiguous starting at 1 (missing tune "
						+ number + ")");
			}
			tunes.add(tune);
		}
		return tunes;
	}

	private SIDScoreIR.TimedScore resolveImportedTune(Path imported) throws IOException {
		String text = Files.readString(imported);
		SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(text));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SIDScoreParser parser = new SIDScoreParser(tokens);
		BaseErrorListener errors = new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String message, RecognitionException cause) {
				throw new IllegalArgumentException(imported + ":" + line + ":" + charPositionInLine
						+ ": " + message, cause);
			}
		};
		lexer.removeErrorListeners();
		lexer.addErrorListener(errors);
		parser.removeErrorListeners();
		parser.addErrorListener(errors);
		ParseTree tree = parser.file();
		ScoreBuildingListener builder = new ScoreBuildingListener(imported);
		ParseTreeWalker.DEFAULT.walk(builder, tree);
		return new SIDScoreIR.Resolver().resolve(builder.buildScoreIR()).timedScore();
	}

	private SIDScoreIR.ScoreIR inlineSongScore(SIDScoreIR.ScoreIR base, SIDScoreIR.SongIR song) {
		Map<String, SIDScoreIR.EffectIR> effects = new LinkedHashMap<>();
		if (song.effects().isEmpty()) effects.putAll(base.effects());
		else effects.putAll(song.effects());
		return new SIDScoreIR.ScoreIR(
				song.title().isPresent() ? song.title() : base.title(),
				song.author().isPresent() ? song.author() : base.author(),
				song.released().isPresent() ? song.released() : base.released(),
				song.tempoBpm().orElse(base.tempoBpm()),
				song.timeSig().isPresent() ? song.timeSig() : base.timeSig(),
				song.system().isPresent() ? song.system() : base.system(),
				song.defaultSwing().orElse(base.defaultSwing()),
				base.tables(), base.instruments(), java.util.Collections.unmodifiableMap(effects),
				song.voices(), Map.of(), Map.of());
	}
}
