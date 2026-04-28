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
package net.resheim.sidscore.ir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;
import net.resheim.sidscore.parser.SIDScoreParserBaseListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScoreBuildingListener extends SIDScoreParserBaseListener {

	public static final class ValidationException extends RuntimeException {
		private static final long serialVersionUID = -8708566624453471822L;
		public final int line;
		public final int col;

		public ValidationException(int line, int col, String message) {
			super("Validation error at " + line + ":" + col + " - " + message);
			this.line = line;
			this.col = col;
		}
	}

	private static final class ImportContext {
		private final Map<Path, ImportResult> cache = new HashMap<>();
		private final Deque<Path> stack = new ArrayDeque<>();
	}

	private static final record ImportResult(Map<String, SIDScoreIR.TableIR> tables,
			Map<String, SIDScoreIR.InstrumentIR> instruments) {
	}

	private static final class ThrowingErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			throw new ValidationException(line, charPositionInLine, msg);
		}
	}

	private final Path sourcePath;
	private final ImportContext importContext;
	private final boolean importMode;

	private Optional<String> title = Optional.empty();
	private Optional<String> author = Optional.empty();
	private Optional<String> released = Optional.empty();
	private Integer tempoBpm = null;
	private Optional<SIDScoreIR.TimeSigIR> timeSig = Optional.empty();
	private Optional<SIDScoreIR.VideoSystem> system = Optional.empty();
	private SIDScoreIR.SwingSetting defaultSwing = new SIDScoreIR.SwingOff();

	private final Map<String, SIDScoreIR.TableIR> tables = new LinkedHashMap<>();
	private final Map<String, SIDScoreIR.InstrumentIR> instruments = new LinkedHashMap<>();
	private final Map<String, SIDScoreIR.EffectIR> effects = new LinkedHashMap<>();
	private final Set<String> effectNames = new LinkedHashSet<>();
	private final Map<Integer, SIDScoreIR.VoiceIR> voices = new LinkedHashMap<>();
	private final Map<Integer, Path> subtunes = new LinkedHashMap<>();
	private final Map<Integer, SIDScoreIR.SongIR> songs = new LinkedHashMap<>();

	private SongBuildState currentSong = null;
	private VoiceBuildState currentVoice = null;

	public ScoreBuildingListener() {
		this(null, new ImportContext(), false);
	}

	public ScoreBuildingListener(Path sourcePath) {
		this(sourcePath, new ImportContext(), false);
	}

	private ScoreBuildingListener(Path sourcePath, ImportContext importContext, boolean importMode) {
		this.sourcePath = sourcePath;
		this.importContext = importContext;
		this.importMode = importMode;
	}

	// ---------------------------
	// Public API
	// ---------------------------
	public SIDScoreIR.ScoreIR buildScoreIR() {
		if (!importMode && tempoBpm == null)
			throw new IllegalStateException("TEMPO is required (spec v0.1)");

		// Post-validate: X legality based on instrument waveform
		validateVoiceMap(voices, null);
		for (var entry : songs.entrySet()) {
			validateVoiceMap(entry.getValue().voices(), entry.getKey());
		}

		return new SIDScoreIR.ScoreIR(title, author, released, tempoBpm, timeSig, system, defaultSwing,
				Collections.unmodifiableMap(tables), Collections.unmodifiableMap(instruments),
				Collections.unmodifiableMap(effects), Collections.unmodifiableMap(voices), Collections.unmodifiableMap(subtunes),
				Collections.unmodifiableMap(songs));
	}

	private void validateVoiceMap(Map<Integer, SIDScoreIR.VoiceIR> voiceMap, Integer songNumber) {
		for (var e : voiceMap.entrySet()) {
			int vIdx = e.getKey();
			SIDScoreIR.VoiceIR v = e.getValue();
			SIDScoreIR.InstrumentIR instr = instruments.get(v.instrumentName());
			if (instr == null) {
				String prefix = songNumber != null ? ("TUNE " + songNumber + " ") : "";
				throw new IllegalStateException(prefix + "VOICE " + vIdx + " references undefined instrument: "
						+ v.instrumentName());
			}
			boolean isNoise = instr.hasWave(SIDScoreIR.Wave.NOISE);
			if (!isNoise && containsHit(v.items())) {
				String prefix = songNumber != null ? ("TUNE " + songNumber + " ") : "";
				throw new IllegalStateException(prefix + "VOICE " + vIdx
						+ " uses X but instrument is not NOISE: " + instr.name());
			}
		}
	}

	private static boolean containsHit(List<SIDScoreIR.VoiceItemIR> items) {
		for (SIDScoreIR.VoiceItemIR it : items) {
			if (it instanceof SIDScoreIR.HitIR)
				return true;
			if (it instanceof SIDScoreIR.LegatoScopeIR leg && containsHit(leg.items()))
				return true;
			if (it instanceof SIDScoreIR.TupletIR tup && containsHit(tup.items()))
				return true;
			if (it instanceof SIDScoreIR.RepeatIR rep && containsHit(rep.items()))
				return true;
		}
		return false;
	}

	private Path baseDir() {
		if (sourcePath == null) {
			return Path.of("").toAbsolutePath().normalize();
		}
		if (Files.isDirectory(sourcePath)) {
			return sourcePath.toAbsolutePath().normalize();
		}
		Path parent = sourcePath.getParent();
		if (parent == null) {
			return Path.of("").toAbsolutePath().normalize();
		}
		return parent.toAbsolutePath().normalize();
	}

	private void importInstrument(String name, String rawPath, Token where) {
		Path resolved = baseDir().resolve(rawPath).normalize();
		ImportResult result = loadImport(resolved, where);

		// Merge tables (allow identical duplicates to avoid repeated-import errors).
		for (var entry : result.tables().entrySet()) {
			SIDScoreIR.TableIR existing = tables.get(entry.getKey());
			if (existing == null) {
				tables.put(entry.getKey(), entry.getValue());
			} else if (!existing.equals(entry.getValue())) {
				throw new ValidationException(posLine(where), posCol(where),
						"Duplicate TABLE name: " + entry.getKey());
			}
		}

		SIDScoreIR.InstrumentIR instr = result.instruments().get(name);
		if (instr == null) {
			throw new ValidationException(posLine(where), posCol(where),
					"Imported file does not define instrument: " + name);
		}
		if (instruments.putIfAbsent(name, instr) != null) {
			throw new ValidationException(posLine(where), posCol(where),
					"Duplicate INSTR name: " + name);
		}
	}

	private ImportResult loadImport(Path path, Token where) {
		Path abs = path.toAbsolutePath().normalize();
		ImportResult cached = importContext.cache.get(abs);
		if (cached != null) {
			return cached;
		}
		if (importContext.stack.contains(abs)) {
			throw new ValidationException(posLine(where), posCol(where),
					"Import cycle detected: " + abs);
		}
		if (!Files.exists(abs)) {
			throw new ValidationException(posLine(where), posCol(where),
					"Import file not found: " + abs);
		}

		importContext.stack.push(abs);
		try {
			String src = Files.readString(abs);
			SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(src));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SIDScoreParser parser = new SIDScoreParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener());

			ParseTree tree = parser.file();
			ScoreBuildingListener listener = new ScoreBuildingListener(abs, importContext, true);
			ParseTreeWalker.DEFAULT.walk(listener, tree);

			ImportResult result = new ImportResult(
					Collections.unmodifiableMap(new LinkedHashMap<>(listener.tables)),
					Collections.unmodifiableMap(new LinkedHashMap<>(listener.instruments)));
			importContext.cache.put(abs, result);
			return result;
		} catch (IOException ex) {
			throw new ValidationException(posLine(where), posCol(where),
					"Failed to read import file: " + abs + " (" + ex.getMessage() + ")");
		} finally {
			importContext.stack.pop();
		}
	}

	// ---------------------------
	// Top-level statements
	// ---------------------------
	@Override
	public void exitTitleStmt(SIDScoreParser.TitleStmtContext ctx) {
		if (importMode)
			return;
		if (currentSong != null) {
			currentSong.title = Optional.of(unquote(ctx.STRING().getText()));
		} else {
			title = Optional.of(unquote(ctx.STRING().getText()));
		}
	}

	@Override
	public void exitAuthorStmt(SIDScoreParser.AuthorStmtContext ctx) {
		if (importMode)
			return;
		if (currentSong != null) {
			currentSong.author = Optional.of(unquote(ctx.STRING().getText()));
		} else {
			author = Optional.of(unquote(ctx.STRING().getText()));
		}
	}

	@Override
	public void exitReleasedStmt(SIDScoreParser.ReleasedStmtContext ctx) {
		if (importMode)
			return;
		if (currentSong != null) {
			currentSong.released = Optional.of(unquote(ctx.STRING().getText()));
		} else {
			released = Optional.of(unquote(ctx.STRING().getText()));
		}
	}

	@Override
	public void exitTempoStmt(SIDScoreParser.TempoStmtContext ctx) {
		if (importMode)
			return;
		int value = Integer.parseInt(ctx.INT().getText());
		if (currentSong != null) {
			currentSong.tempoBpm = OptionalInt.of(value);
		} else {
			tempoBpm = value;
		}
	}

	@Override
	public void exitTimeStmt(SIDScoreParser.TimeStmtContext ctx) {
		if (importMode)
			return;
		int num = Integer.parseInt(ctx.INT(0).getText());
		int den = Integer.parseInt(ctx.INT(1).getText());
		SIDScoreIR.TimeSigIR value = new SIDScoreIR.TimeSigIR(num, den);
		if (currentSong != null) {
			currentSong.timeSig = Optional.of(value);
		} else {
			timeSig = Optional.of(value);
		}
	}

	@Override
	public void exitSystemStmt(SIDScoreParser.SystemStmtContext ctx) {
		if (importMode)
			return;
		SIDScoreIR.VideoSystem value = ctx.PAL() != null ? SIDScoreIR.VideoSystem.PAL : SIDScoreIR.VideoSystem.NTSC;
		if (currentSong != null) {
			if (currentSong.system.isPresent()) {
				throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
						"SYSTEM already specified in TUNE " + currentSong.number);
			}
			currentSong.system = Optional.of(value);
			return;
		}
		if (system.isPresent()) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"SYSTEM already specified");
		}
		system = Optional.of(value);
	}

	@Override
	public void exitImportStmt(SIDScoreParser.ImportStmtContext ctx) {
		if (importMode) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"IMPORT is not allowed in imported instrument files");
		}
		if (currentSong != null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"IMPORT is not allowed inside TUNE blocks");
		}
		String rawPath = unquote(ctx.STRING().getText());
		int songNumber = Integer.parseInt(ctx.INT().getText());
		if (songNumber < 2 || songNumber > 255) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"IMPORT AS number must be in range 2..255");
		}
		Path resolved = baseDir().resolve(rawPath).normalize().toAbsolutePath();
		if (!Files.exists(resolved)) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Imported score file not found: " + resolved);
		}
		Path previous = subtunes.putIfAbsent(songNumber, resolved);
		if (previous != null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Duplicate IMPORT AS " + songNumber + " for files " + previous + " and " + resolved);
		}
	}

	@Override
	public void enterSongBlock(SIDScoreParser.SongBlockContext ctx) {
		if (importMode) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"TUNE is not allowed in imported instrument files");
		}
		if (currentSong != null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Nested TUNE blocks are not allowed");
		}
		int number = Integer.parseInt(ctx.INT().getText());
		if (number < 2 || number > 255) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"TUNE number must be in range 2..255");
		}
		if (songs.containsKey(number)) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Duplicate TUNE number: " + number);
		}
		currentSong = new SongBuildState(number);
	}

	@Override
	public void exitSongBlock(SIDScoreParser.SongBlockContext ctx) {
		if (currentSong == null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Internal error: TUNE state missing");
		}
		if (currentSong.voices.isEmpty() && currentSong.effects.isEmpty()) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"TUNE " + currentSong.number + " must contain at least one VOICE or EFFECT");
		}
		SIDScoreIR.SongIR song = new SIDScoreIR.SongIR(
				currentSong.title,
				currentSong.author,
				currentSong.released,
				currentSong.tempoBpm,
				currentSong.timeSig,
				currentSong.system,
				currentSong.defaultSwing,
				Collections.unmodifiableMap(new LinkedHashMap<>(currentSong.effects)),
				Collections.unmodifiableMap(new LinkedHashMap<>(currentSong.voices)));
		songs.put(currentSong.number, song);
		currentSong = null;
	}

	@Override
	public void exitTableStmt(SIDScoreParser.TableStmtContext ctx) {
		String typeRaw = ctx.ID(0).getText();
		String name = ctx.ID(1).getText();
		String type = typeRaw.toLowerCase(Locale.ROOT);

		boolean isPw = "pw".equals(type);
		boolean isWave = "wave".equals(type);
		boolean isGate = "gate".equals(type);
		boolean isPitch = "pitch".equals(type);
		boolean isFilter = "filter".equals(type);
		if (!isPw && !isWave && !isGate && !isPitch && !isFilter) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"TABLE type '" + typeRaw
							+ "' not supported (only 'pw', 'wave', 'gate', 'pitch', or 'filter' are supported)");
		}

		List<SIDScoreIR.TableStepIR> steps = new ArrayList<>();
		boolean loop = false;

		List<SIDScoreParser.TableStepContext> stepCtxs = ctx.tableStep();
		for (int i = 0; i < stepCtxs.size(); i++) {
			SIDScoreParser.TableStepContext step = stepCtxs.get(i);
			if (step.LOOP() != null) {
				if (i != stepCtxs.size() - 1) {
					throw new ValidationException(posLine(step.getStart()), posCol(step.getStart()),
							"LOOP must be the final entry in TABLE " + name);
				}
				loop = true;
				continue;
			}

			boolean isCtrl = step.tableCtrl() != null;
			SIDScoreParser.TableValueContext valCtx = step.tableValue();
			int value = 0;
			boolean waveSet = false;
			SIDScoreIR.NoteMode noteMode = SIDScoreIR.NoteMode.NONE;
			int noteValue = 0;
			SIDScoreIR.TriState gateState = SIDScoreIR.TriState.UNSET;
			SIDScoreIR.TriState ringState = SIDScoreIR.TriState.UNSET;
			SIDScoreIR.TriState syncState = SIDScoreIR.TriState.UNSET;
			boolean reset = false;

			if (isCtrl && !isWave) {
				throw new ValidationException(posLine(step.getStart()), posCol(step.getStart()),
						"TABLE " + type + " does not support control fields");
			}

			if (isWave) {
				if (isCtrl) {
					for (SIDScoreParser.TableCtrlItemContext item : step.tableCtrl().tableCtrlItem()) {
						if (item.WAVE() != null) {
							if (item.waveList() == null) {
								throw new ValidationException(posLine(item.getStart()), posCol(item.getStart()),
										"TABLE wave WAVE= expects waveform values");
							}
							value = waveMaskFromWaveList(item.waveList());
							if (value == 0) {
								throw new ValidationException(posLine(item.getStart()), posCol(item.getStart()),
										"TABLE wave WAVE= must include at least one waveform");
							}
							waveSet = true;
						} else if (item.NOTEK() != null) {
							SIDScoreParser.NoteSpecContext ns = item.noteSpec();
							if (ns.OFF() != null) {
								noteMode = SIDScoreIR.NoteMode.NONE;
							} else if (ns.NOTE() != null) {
								noteMode = SIDScoreIR.NoteMode.ABS;
								noteValue = parsePitchToken(ns.NOTE().getText(), ns.getStart());
							} else if (ns.signedInt() != null) {
								noteMode = SIDScoreIR.NoteMode.REL;
								noteValue = parseSignedInt(ns.signedInt());
							}
						} else if (item.GATE() != null) {
							gateState = item.onOff().ON() != null ? SIDScoreIR.TriState.ON
									: SIDScoreIR.TriState.OFF;
						} else if (item.RING() != null) {
							ringState = item.onOff().ON() != null ? SIDScoreIR.TriState.ON
									: SIDScoreIR.TriState.OFF;
						} else if (item.SYNC() != null) {
							syncState = item.onOff().ON() != null ? SIDScoreIR.TriState.ON
									: SIDScoreIR.TriState.OFF;
						} else if (item.RESET() != null) {
							reset = true;
						}
					}
				} else {
					if (valCtx.waveList() == null) {
						throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
								"TABLE wave expects waveform values");
					}
					value = waveMaskFromWaveList(valCtx.waveList());
					if (value == 0) {
						throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
								"TABLE wave value must include at least one waveform");
					}
					waveSet = true;
				}
			} else if (isGate) {
				if (valCtx.ON() == null && valCtx.OFF() == null) {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE gate expects ON or OFF");
				}
				value = valCtx.ON() != null ? 1 : 0;
			} else if (isPitch) {
				if (valCtx.HEX() != null) {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE pitch expects signed integer semitone offsets");
				}
				if (valCtx.signedInt() != null) {
					value = parseSignedInt(valCtx.signedInt());
				} else if (valCtx.INT() != null) {
					value = Integer.parseInt(valCtx.INT().getText());
				} else {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE pitch expects signed integer semitone offsets");
				}
			} else if (isFilter) {
				if (valCtx.signedInt() != null
						&& (valCtx.signedInt().MINUS() != null || valCtx.signedInt().PLUS() != null)) {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE filter expects unsigned integer cutoff values");
				}
				if (valCtx.HEX() != null) {
					value = parseHexValue(valCtx.HEX().getText(), valCtx.getStart(), "TABLE filter value");
				} else if (valCtx.signedInt() != null) {
					value = Integer.parseInt(valCtx.signedInt().INT().getText());
				} else if (valCtx.INT() != null) {
					value = Integer.parseInt(valCtx.INT().getText());
				} else {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE filter expects integer cutoff values");
				}
				if (value < 0 || value > 0x07FF) {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE filter value out of range 0..2047");
				}
			} else {
				if (valCtx.signedInt() != null
						&& (valCtx.signedInt().MINUS() != null || valCtx.signedInt().PLUS() != null)) {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE pw expects unsigned values (decimal or $hex)");
				}
				if (valCtx.HEX() != null) {
					value = parseHexValue(valCtx.HEX().getText(), valCtx.getStart(), "TABLE pw value");
				} else if (valCtx.signedInt() != null) {
					value = Integer.parseInt(valCtx.signedInt().INT().getText());
				} else {
					value = Integer.parseInt(valCtx.INT().getText());
				}
				if (value > 0x0FFF) {
					throw new ValidationException(posLine(valCtx.getStart()), posCol(valCtx.getStart()),
							"TABLE pw value out of range 0..4095 ($0000..$0FFF)");
				}
			}

			SIDScoreParser.TableDurationContext durCtx = step.tableDuration();
			boolean hold = false;
			int duration = 0;
			if (durCtx != null) {
				hold = durCtx.HOLD() != null;
				if (!hold) {
					duration = Integer.parseInt(durCtx.INT().getText());
					if (duration < 1) {
						throw new ValidationException(posLine(durCtx.getStart()), posCol(durCtx.getStart()),
								"TABLE " + type + " duration must be >= 1");
					}
				}
			} else if (step.HOLD() != null) {
				hold = true;
			} else {
				throw new ValidationException(posLine(step.getStart()), posCol(step.getStart()),
						"TABLE " + type + " step missing duration");
			}

			if (hold && i != stepCtxs.size() - 1) {
				Token where = durCtx != null ? durCtx.getStart() : step.getStart();
				throw new ValidationException(posLine(where), posCol(where),
						"HOLD must be the final entry in TABLE " + name);
			}

			steps.add(new SIDScoreIR.TableStepIR(value, duration, hold, waveSet, noteMode, noteValue,
					gateState, ringState, syncState, reset));
		}

		if (steps.isEmpty()) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"TABLE " + name + " has no steps");
		}
		if (steps.size() > 63) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"TABLE " + name + " has too many steps (max 63)");
		}

		SIDScoreIR.TableType tableType = isWave ? SIDScoreIR.TableType.WAVE
				: (isGate ? SIDScoreIR.TableType.GATE
						: (isPitch ? SIDScoreIR.TableType.PITCH
								: (isFilter ? SIDScoreIR.TableType.FILTER : SIDScoreIR.TableType.PW)));
		var table = new SIDScoreIR.TableIR(name, tableType, List.copyOf(steps), loop);
		if (tables.putIfAbsent(name, table) != null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Duplicate TABLE name: " + name);
		}
	}

	@Override
	public void exitEffectStmt(SIDScoreParser.EffectStmtContext ctx) {
		String name = ctx.ID().getText();
		if (!effectNames.add(name)) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Duplicate EFFECT name: " + name);
		}

		OptionalInt preferredVoice = OptionalInt.empty();
		boolean voiceSeen = false;
		Integer lengthTicks = null;
		Integer priority = 0;
		SIDScoreIR.EffectRetriggerMode retriggerMode = SIDScoreIR.EffectRetriggerMode.RESTART;
		boolean prioritySeen = false;
		boolean retriggerSeen = false;
		List<SIDScoreIR.EffectStepIR> steps = new ArrayList<>();

		for (SIDScoreParser.EffectBodyStmtContext body : ctx.effectBodyStmt()) {
			if (body.effectVoiceStmt() != null) {
				if (voiceSeen) {
					throw new ValidationException(posLine(body.getStart()), posCol(body.getStart()),
							"EFFECT " + name + " has duplicate VOICE");
				}
				voiceSeen = true;
				SIDScoreParser.EffectVoiceContext voice = body.effectVoiceStmt().effectVoice();
				if (voice.ANY() != null) {
					preferredVoice = OptionalInt.empty();
				} else {
					int value = Integer.parseInt(voice.INT().getText());
					if (value < 1 || value > 3) {
						throw new ValidationException(posLine(voice.getStart()), posCol(voice.getStart()),
								"Invalid effect voice " + value + " for EFFECT " + name);
					}
					preferredVoice = OptionalInt.of(value);
				}
			} else if (body.effectLengthStmt() != null) {
				if (lengthTicks != null) {
					throw new ValidationException(posLine(body.getStart()), posCol(body.getStart()),
							"EFFECT " + name + " has duplicate LENGTH");
				}
				lengthTicks = Integer.parseInt(body.effectLengthStmt().INT().getText());
				if (lengthTicks < 1) {
					throw new ValidationException(posLine(body.getStart()), posCol(body.getStart()),
							"Invalid effect length " + lengthTicks + " for EFFECT " + name);
				}
			} else if (body.effectPriorityStmt() != null) {
				if (prioritySeen) {
					throw new ValidationException(posLine(body.getStart()), posCol(body.getStart()),
							"EFFECT " + name + " has duplicate PRIORITY");
				}
				prioritySeen = true;
				priority = Integer.parseInt(body.effectPriorityStmt().INT().getText());
				if (priority < 0 || priority > 255) {
					throw new ValidationException(posLine(body.getStart()), posCol(body.getStart()),
							"EFFECT " + name + " PRIORITY out of range 0..255");
				}
			} else if (body.effectRetriggerStmt() != null) {
				if (retriggerSeen) {
					throw new ValidationException(posLine(body.getStart()), posCol(body.getStart()),
							"EFFECT " + name + " has duplicate RETRIGGER");
				}
				retriggerSeen = true;
				retriggerMode = parseEffectRetriggerMode(body.effectRetriggerStmt().effectRetriggerMode());
			} else if (body.effectStep() != null) {
				parseEffectStep(body.effectStep(), name, steps);
			}
		}

		if (!voiceSeen) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"EFFECT " + name + " missing VOICE");
		}
		if (lengthTicks == null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"EFFECT " + name + " missing LENGTH");
		}
		if (steps.isEmpty()) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"EFFECT " + name + " has no steps");
		}
		validateEffectPitchFreqConflicts(name, steps, ctx.getStart());

		var effect = new SIDScoreIR.EffectIR(name, preferredVoice, lengthTicks, priority, retriggerMode,
				List.copyOf(steps));
		if (currentSong != null) {
			currentSong.effects.put(name, effect);
		} else {
			effects.put(name, effect);
		}
	}

	@Override
	public void exitSwingStmt(SIDScoreParser.SwingStmtContext ctx) {
		if (importMode)
			return;
		SIDScoreIR.SwingSetting s = parseSwingSetting(ctx);
		if (currentVoice != null) {
			currentVoice.emit(new SIDScoreIR.SetSwingIR(s));
			return;
		}
		if (currentSong != null) {
			currentSong.defaultSwing = Optional.of(s);
		} else {
			defaultSwing = s;
		}
	}

	@Override
	public void exitInstrStmt(SIDScoreParser.InstrStmtContext ctx) {
		String name = ctx.ID().getText();
		if (ctx.STRING() != null) {
			String rawPath = unquote(ctx.STRING().getText());
			importInstrument(name, rawPath, ctx.getStart());
			return;
		}

		int waveMask = 0;
		SIDScoreIR.AdsrIR adsr = null;
		OptionalInt pw = OptionalInt.empty();
		int hiPulse = -1;
		int lowPulse = -1;
		OptionalInt pwMin = OptionalInt.empty();
		OptionalInt pwMax = OptionalInt.empty();
		int pwSweep = 0;
		Optional<String> waveSeq = Optional.empty();
		Optional<String> pwSeq = Optional.empty();
		Optional<String> gateSeq = Optional.empty();
		Optional<String> pitchSeq = Optional.empty();
		int filterModeMask = 0;
		OptionalInt filterCutoff = OptionalInt.empty();
		OptionalInt filterRes = OptionalInt.empty();
		Optional<String> filterSeq = Optional.empty();
		SIDScoreIR.InstrumentGateMode gateMode = SIDScoreIR.InstrumentGateMode.RETRIGGER;
		int gateMin = 0;
		boolean sync = false;
		boolean ring = false;

		for (SIDScoreParser.InstrParamContext p : ctx.instrParam()) {
			if (p.WAVE() != null) {
				for (var w : p.waveList().WAVEVAL()) {
					waveMask |= SIDScoreIR.Wave.valueOf(w.getText()).mask;
				}
			} else if (p.ADSR() != null) {
				int a = Integer.parseInt(p.INT(0).getText());
				int d = Integer.parseInt(p.INT(1).getText());
				int s = Integer.parseInt(p.INT(2).getText());
				int r = Integer.parseInt(p.INT(3).getText());
				adsr = new SIDScoreIR.AdsrIR(a, d, s, r);
			} else if (p.PW() != null) {
				int v;
				if (p.HEX() != null) {
					v = parseHexValue(p.HEX().getText(), p.getStart(), "PW");
				} else {
					v = Integer.parseInt(p.INT(0).getText());
				}
				if (v > 0x0FFF) {
					throw new ValidationException(posLine(p.getStart()), posCol(p.getStart()),
							"PW out of range 0..4095 ($0000..$0FFF)");
				}
				pw = OptionalInt.of(v);
			} else if (p.HIPULSE() != null) {
				int v;
				if (p.HEX() != null) {
					v = parseHexValue(p.HEX().getText(), p.getStart(), "HIPULSE");
				} else {
					v = Integer.parseInt(p.INT(0).getText());
				}
				if (v < 0 || v > 0x0F) {
					throw new ValidationException(posLine(p.getStart()), posCol(p.getStart()),
							"HIPULSE out of range 0..15 ($0..$F)");
				}
				hiPulse = v;
			} else if (p.LOWPULSE() != null) {
				int v;
				if (p.HEX() != null) {
					v = parseHexValue(p.HEX().getText(), p.getStart(), "LOWPULSE");
				} else {
					v = Integer.parseInt(p.INT(0).getText());
				}
				if (v < 0 || v > 0xFF) {
					throw new ValidationException(posLine(p.getStart()), posCol(p.getStart()),
							"LOWPULSE out of range 0..255 ($00..$FF)");
				}
				lowPulse = v;
			} else if (p.FILTER() != null) {
				SIDScoreParser.FilterSpecContext spec = p.filterSpec();
				if (spec.OFF() != null) {
					filterModeMask = 0;
				} else if (spec.filterList() != null) {
					int mask = 0;
					for (SIDScoreParser.FilterModeContext fm : spec.filterList().filterMode()) {
						if (fm.LP() != null) {
							mask |= SIDScoreIR.FilterMode.LP.mask;
						} else if (fm.BP() != null) {
							mask |= SIDScoreIR.FilterMode.BP.mask;
						} else if (fm.HP() != null) {
							mask |= SIDScoreIR.FilterMode.HP.mask;
						}
					}
					filterModeMask = mask;
				}
			} else if (p.CUTOFF() != null) {
				int v;
				if (p.HEX() != null) {
					v = parseHexValue(p.HEX().getText(), p.getStart(), "CUTOFF");
				} else {
					v = Integer.parseInt(p.INT(0).getText());
				}
				if (v < 0 || v > 0x07FF) {
					throw new ValidationException(posLine(p.getStart()), posCol(p.getStart()),
							"CUTOFF out of range 0..2047");
				}
				filterCutoff = OptionalInt.of(v);
			} else if (p.RES() != null) {
				int v = Integer.parseInt(p.INT(0).getText());
				if (v < 0 || v > 15) {
					throw new ValidationException(posLine(p.getStart()), posCol(p.getStart()),
							"RES out of range 0..15");
				}
				filterRes = OptionalInt.of(v);
			} else if (p.WAVESEQ() != null) {
				waveSeq = Optional.of(p.ID().getText());
			} else if (p.GATE() != null) {
				gateMode = p.gateMode().LEGATO() != null
						? SIDScoreIR.InstrumentGateMode.LEGATO
						: SIDScoreIR.InstrumentGateMode.RETRIGGER;
			} else if (p.GATEMIN() != null) {
				gateMin = Integer.parseInt(p.INT(0).getText());
			} else if (p.PWMIN() != null) {
				int v;
				if (p.HEX() != null) {
					v = parseHexValue(p.HEX().getText(), p.getStart(), "PWMIN");
				} else {
					v = Integer.parseInt(p.INT(0).getText());
				}
				if (v > 0x0FFF) {
					throw new ValidationException(posLine(p.getStart()), posCol(p.getStart()),
							"PWMIN out of range 0..4095 ($0000..$0FFF)");
				}
				pwMin = OptionalInt.of(v);
			} else if (p.PWMAX() != null) {
				int v;
				if (p.HEX() != null) {
					v = parseHexValue(p.HEX().getText(), p.getStart(), "PWMAX");
				} else {
					v = Integer.parseInt(p.INT(0).getText());
				}
				if (v > 0x0FFF) {
					throw new ValidationException(posLine(p.getStart()), posCol(p.getStart()),
							"PWMAX out of range 0..4095 ($0000..$0FFF)");
				}
				pwMax = OptionalInt.of(v);
			} else if (p.PWSWEEP() != null) {
				pwSweep = parseSignedInt(p.signedInt());
			} else if (p.PWSEQ() != null) {
				pwSeq = Optional.of(p.ID().getText());
			} else if (p.FILTERSEQ() != null) {
				filterSeq = Optional.of(p.ID().getText());
			} else if (p.GATESEQ() != null) {
				gateSeq = Optional.of(p.ID().getText());
			} else if (p.PITCHSEQ() != null) {
				pitchSeq = Optional.of(p.ID().getText());
			} else if (p.SYNC() != null) {
				sync = p.onOff().ON() != null;
			} else if (p.RING() != null) {
				ring = p.onOff().ON() != null;
			}
		}

		if (waveMask == 0)
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()), "INSTR missing WAVE=");
		if (adsr == null)
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()), "INSTR missing ADSR=");

		if (pwMin.isPresent() && pwMax.isPresent() && pwMin.getAsInt() > pwMax.getAsInt()) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"PWMIN must be <= PWMAX");
		}
		if (gateMin < 0) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"GATEMIN must be >= 0");
		}
		if (hiPulse >= 0 || lowPulse >= 0) {
			int mergedPw = pw.isPresent() ? (pw.getAsInt() & 0x0FFF) : 0x0800;
			if (lowPulse >= 0) {
				mergedPw = (mergedPw & 0x0F00) | (lowPulse & 0xFF);
			}
			if (hiPulse >= 0) {
				mergedPw = (mergedPw & 0x00FF) | ((hiPulse & 0x0F) << 8);
			}
			pw = OptionalInt.of(mergedPw & 0x0FFF);
		}

		var instr = new SIDScoreIR.InstrumentIR(name, waveMask, adsr, pw, pwMin, pwMax, pwSweep, waveSeq, pwSeq,
				gateSeq, pitchSeq, filterModeMask, filterCutoff, filterRes, filterSeq, gateMode, gateMin, sync, ring);
		if (instruments.putIfAbsent(name, instr) != null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Duplicate INSTR name: " + name);
		}
	}

	// ---------------------------
	// VOICE block
	// ---------------------------
	@Override
	public void enterVoiceBlock(SIDScoreParser.VoiceBlockContext ctx) {
		if (importMode)
			return;
		int idx = Integer.parseInt(ctx.INT().getText());
		String instrName = ctx.ID().getText();
		currentVoice = new VoiceBuildState(idx, instrName);
	}

	@Override
	public void exitVoiceBlock(SIDScoreParser.VoiceBlockContext ctx) {
		if (importMode)
			return;
		VoiceBuildState v = currentVoice;
		currentVoice = null;

		v.assertScopesClosed(ctx.getStop());

		var voiceIR = new SIDScoreIR.VoiceIR(v.voiceIndex, v.instrumentName, List.copyOf(v.rootItems));
		Map<Integer, SIDScoreIR.VoiceIR> target = currentSong != null ? currentSong.voices : voices;
		if (target.putIfAbsent(v.voiceIndex, voiceIR) != null) {
			String where = currentSong != null ? (" in TUNE " + currentSong.number) : "";
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Duplicate VOICE " + v.voiceIndex + where);
		}
	}

	// ---------------------------
	// voiceItem leaves
	// ---------------------------
	@Override
	public void exitVoiceItem(SIDScoreParser.VoiceItemContext ctx) {
		if (currentVoice == null)
			return;

		if (ctx.OCTAVE() != null) {
			int o = parsePrefixedInt(ctx.OCTAVE().getText(), 'O');
			currentVoice.setOctave(o);
			return;
		}
		if (ctx.LENGTH() != null) {
			int denom = parsePrefixedInt(ctx.LENGTH().getText(), 'L');
			currentVoice.emit(new SIDScoreIR.SetDefaultLengthIR(SIDScoreIR.LengthIR.fromDenom(denom)));
			return;
		}
		if (ctx.GT() != null) {
			currentVoice.octaveUp();
			return;
		}
		if (ctx.LT() != null) {
			currentVoice.octaveDown();
			return;
		}

		if (ctx.AMP() != null) {
			currentVoice.emit(new SIDScoreIR.TieIR());
			return;
		}

		if (ctx.noteOrRestOrHit() != null) {
			var n = ctx.noteOrRestOrHit();
			if (n.NOTE() != null) {
				currentVoice.emit(parseNoteToken(n.NOTE().getText(), currentVoice.octave));
				return;
			}
			if (n.REST() != null) {
				currentVoice.emit(parseRestToken(n.REST().getText()));
				return;
			}
			if (n.HIT() != null) {
				currentVoice.emit(parseHitToken(n.HIT().getText()));
				return;
			}
		}
	}

	// ---------------------------
	// Scoped constructs
	// ---------------------------
	private enum ScopeKind {
		LEGATO, TUPLET, REPEAT
	}

	@Override
	public void enterLegatoScope(SIDScoreParser.LegatoScopeContext ctx) {
		if (currentVoice != null)
			currentVoice.pushScope(ScopeKind.LEGATO);
	}

	@Override
	public void exitLegatoScope(SIDScoreParser.LegatoScopeContext ctx) {
		if (currentVoice == null)
			return;
		List<SIDScoreIR.VoiceItemIR> body = currentVoice.popScope(ScopeKind.LEGATO, ctx.getStop());
		currentVoice.emit(new SIDScoreIR.LegatoScopeIR(body));
	}

	@Override
	public void enterTuplet(SIDScoreParser.TupletContext ctx) {
		if (currentVoice != null)
			currentVoice.pushScope(ScopeKind.TUPLET);
	}

	@Override
	public void exitTuplet(SIDScoreParser.TupletContext ctx) {
		if (currentVoice == null)
			return;

		List<SIDScoreIR.VoiceItemIR> body = currentVoice.popScope(ScopeKind.TUPLET, ctx.getStop());

		// v0.1 validation: only NOTE/REST/HIT and '&' allowed inside T{ }
		for (SIDScoreIR.VoiceItemIR it : body) {
			boolean ok = it instanceof SIDScoreIR.NoteIR || it instanceof SIDScoreIR.RestIR
					|| it instanceof SIDScoreIR.HitIR || it instanceof SIDScoreIR.TieIR;
			if (!ok) {
				throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
						"Tuplet may only contain NOTE/REST/HIT and '&' (no state/scopes)");
			}
		}

		int logical = countTupletLogicalEvents(body, ctx.getStart());
		if (logical != 3) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Tuplet must contain exactly 3 logical events (after tie-merge), found " + logical);
		}

		currentVoice.emit(new SIDScoreIR.TupletIR(body));
	}

	@Override
	public void enterRepeat(SIDScoreParser.RepeatContext ctx) {
		if (currentVoice != null)
			currentVoice.pushScope(ScopeKind.REPEAT);
	}

	@Override
	public void exitRepeat(SIDScoreParser.RepeatContext ctx) {
		if (currentVoice == null)
			return;

		String repeatEnd = ctx.REPEAT_END().getText(); // ")xN"
		int times = Integer.parseInt(repeatEnd.substring(2));
		if (times < 1) {
			Token t = ctx.REPEAT_END().getSymbol();
			throw new ValidationException(posLine(t), posCol(t), "Repeat count must be >= 1, got " + times);
		}

		List<SIDScoreIR.VoiceItemIR> body = currentVoice.popScope(ScopeKind.REPEAT, ctx.getStop());
		currentVoice.emit(new SIDScoreIR.RepeatIR(times, body));
	}

	// ---------------------------
	// Tuplet logical counting with tie-merge (strict v0.1)
	// ---------------------------
	private static int countTupletLogicalEvents(List<SIDScoreIR.VoiceItemIR> items, Token where) {
		int count = 0;
		for (int i = 0; i < items.size(); i++) {
			SIDScoreIR.VoiceItemIR it = items.get(i);

			if (it instanceof SIDScoreIR.TieIR) {
				// tie must be between two notes with identical pitch
				if (i == 0 || !(items.get(i - 1) instanceof SIDScoreIR.NoteIR prev)) {
					throw new ValidationException(posLine(where), posCol(where),
							"Tie '&' inside tuplet must follow NOTE");
				}
				if (i + 1 >= items.size() || !(items.get(i + 1) instanceof SIDScoreIR.NoteIR next)) {
					throw new ValidationException(posLine(where), posCol(where),
							"Tie '&' inside tuplet must precede NOTE");
				}
				int pm = prev.pitch().toMidi();
				int nm = next.pitch().toMidi();
				if (pm != nm) {
					throw new ValidationException(posLine(where), posCol(where),
							"Tie '&' inside tuplet requires identical pitch");
				}
				// tie merges; consume next note so it does not become its own logical event
				i++;
				continue;
			}

			if (it instanceof SIDScoreIR.NoteIR || it instanceof SIDScoreIR.RestIR || it instanceof SIDScoreIR.HitIR) {
				count++;
			}
		}
		return count;
	}

	// ---------------------------
	// Effect parsing helpers
	// ---------------------------
	private static void parseEffectStep(SIDScoreParser.EffectStepContext ctx, String effectName,
			List<SIDScoreIR.EffectStepIR> out) {
		if (ctx.effectAssignment() != null) {
			int tick = ctx.effectTick() != null ? parseEffectTick(ctx.effectTick(), effectName) : 0;
			out.add(parseEffectAssignment(ctx.effectAssignment(), tick));
			return;
		}
		if (ctx.effectSweep() != null) {
			out.add(parseEffectSweep(ctx.effectSweep(), effectName));
			return;
		}
		if (ctx.effectGroup() != null) {
			SIDScoreParser.EffectGroupContext group = ctx.effectGroup();
			int tick = parseNonNegativeInt(group.INT().getText(), group.INT().getSymbol(),
					"EFFECT " + effectName + " group tick");
			if (group.effectAssignment().isEmpty()) {
				throw new ValidationException(posLine(group.getStart()), posCol(group.getStart()),
						"EFFECT " + effectName + " has empty AT/FRAME group");
			}
			for (SIDScoreParser.EffectAssignmentContext assignment : group.effectAssignment()) {
				out.add(parseEffectAssignment(assignment, tick));
			}
		}
	}

	private static int parseEffectTick(SIDScoreParser.EffectTickContext ctx, String effectName) {
		return parseNonNegativeInt(ctx.INT().getText(), ctx.INT().getSymbol(),
				"EFFECT " + effectName + " tick");
	}

	private static SIDScoreIR.EffectAssignmentIR parseEffectAssignment(SIDScoreParser.EffectAssignmentContext ctx,
			int tick) {
		if (ctx.WAVE() != null) {
			int mask = waveMaskFromWaveList(ctx.waveList());
			if (mask == 0) {
				throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
						"EFFECT WAVE must include at least one waveform");
			}
			return effectAssignment(tick, SIDScoreIR.EffectParameter.WAVE, SIDScoreIR.EffectValueIR.wave(mask));
		}
		if (ctx.GATE() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.GATE, parseEffectOnOff(ctx.onOff()));
		}
		if (ctx.SYNC() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.SYNC, parseEffectOnOff(ctx.onOff()));
		}
		if (ctx.RING() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.RING, parseEffectOnOff(ctx.onOff()));
		}
		if (ctx.RESET() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.RESET, SIDScoreIR.EffectValueIR.none());
		}
		if (ctx.PITCH() != null) {
			int midi = parseEffectPitch(ctx.NOTE().getText(), ctx.NOTE().getSymbol(), "PITCH");
			return effectAssignment(tick, SIDScoreIR.EffectParameter.PITCH, SIDScoreIR.EffectValueIR.pitch(midi));
		}
		if (ctx.FREQ() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.FREQ,
					parseEffectNumericValue(ctx.numericValue(), "FREQ", 0, 0xFFFF));
		}
		if (ctx.PW() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.PW,
					parseEffectNumericValue(ctx.numericValue(), "PW", 0, 0x0FFF));
		}
		if (ctx.HIPULSE() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.HIPULSE,
					parseEffectNumericValue(ctx.numericValue(), "HIPULSE", 0, 0x0F));
		}
		if (ctx.LOWPULSE() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.LOWPULSE,
					parseEffectNumericValue(ctx.numericValue(), "LOWPULSE", 0, 0xFF));
		}
		if (ctx.ADSR() != null) {
			SIDScoreIR.AdsrIR adsr = new SIDScoreIR.AdsrIR(
					parseEffectInt(ctx.INT(0).getText(), ctx.INT(0).getSymbol(), "ADSR attack", 0, 15),
					parseEffectInt(ctx.INT(1).getText(), ctx.INT(1).getSymbol(), "ADSR decay", 0, 15),
					parseEffectInt(ctx.INT(2).getText(), ctx.INT(2).getSymbol(), "ADSR sustain", 0, 15),
					parseEffectInt(ctx.INT(3).getText(), ctx.INT(3).getSymbol(), "ADSR release", 0, 15));
			return effectAssignment(tick, SIDScoreIR.EffectParameter.ADSR, SIDScoreIR.EffectValueIR.adsr(adsr));
		}
		if (ctx.ATTACK() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.ATTACK,
					parseEffectIntValue(ctx.INT(0), "ATTACK", 0, 15));
		}
		if (ctx.DECAY() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.DECAY,
					parseEffectIntValue(ctx.INT(0), "DECAY", 0, 15));
		}
		if (ctx.SUSTAIN() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.SUSTAIN,
					parseEffectIntValue(ctx.INT(0), "SUSTAIN", 0, 15));
		}
		if (ctx.RELEASE() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.RELEASE,
					parseEffectIntValue(ctx.INT(0), "RELEASE", 0, 15));
		}
		if (ctx.FILTER() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.FILTER,
					SIDScoreIR.EffectValueIR.filter(filterMaskFromFilterSpec(ctx.filterSpec())));
		}
		if (ctx.FILTERROUTE() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.FILTERROUTE,
					parseEffectNumericValue(ctx.numericValue(), "FILTERROUTE", 0, 0x0F));
		}
		if (ctx.CUTOFF() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.CUTOFF,
					parseEffectNumericValue(ctx.numericValue(), "CUTOFF", 0, 0x07FF));
		}
		if (ctx.RES() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.RES,
					parseEffectIntValue(ctx.INT(0), "RES", 0, 15));
		}
		if (ctx.VOLUME() != null) {
			return effectAssignment(tick, SIDScoreIR.EffectParameter.VOLUME,
					parseEffectIntValue(ctx.INT(0), "VOLUME", 0, 15));
		}
		throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
				"Invalid effect parameter");
	}

	private static SIDScoreIR.EffectAssignmentIR effectAssignment(int tick, SIDScoreIR.EffectParameter parameter,
			SIDScoreIR.EffectValueIR value) {
		return new SIDScoreIR.EffectAssignmentIR(tick, parameter, value);
	}

	private static SIDScoreIR.EffectSweepIR parseEffectSweep(SIDScoreParser.EffectSweepContext ctx,
			String effectName) {
		SIDScoreIR.EffectParameter parameter = parseEffectSweepParameter(ctx.effectSweepParam());
		SIDScoreIR.EffectValueIR from = parseEffectSweepValue(ctx.effectSweepValue(0), parameter);
		SIDScoreIR.EffectValueIR to = parseEffectSweepValue(ctx.effectSweepValue(1), parameter);
		int duration = parseNonNegativeInt(ctx.INT().getText(), ctx.INT().getSymbol(),
				"EFFECT " + effectName + " sweep duration");
		if (duration < 1) {
			throw new ValidationException(posLine(ctx.INT().getSymbol()), posCol(ctx.INT().getSymbol()),
					"EFFECT " + effectName + " sweep duration must be >= 1");
		}
		return new SIDScoreIR.EffectSweepIR(parameter, from, to, duration,
				parseEffectSweepCurve(ctx.effectSweepCurve()));
	}

	private static SIDScoreIR.EffectParameter parseEffectSweepParameter(SIDScoreParser.EffectSweepParamContext ctx) {
		if (ctx.PITCH() != null)
			return SIDScoreIR.EffectParameter.PITCH;
		if (ctx.FREQ() != null)
			return SIDScoreIR.EffectParameter.FREQ;
		if (ctx.PW() != null)
			return SIDScoreIR.EffectParameter.PW;
		if (ctx.CUTOFF() != null)
			return SIDScoreIR.EffectParameter.CUTOFF;
		return SIDScoreIR.EffectParameter.VOLUME;
	}

	private static SIDScoreIR.EffectValueIR parseEffectSweepValue(SIDScoreParser.EffectSweepValueContext ctx,
			SIDScoreIR.EffectParameter parameter) {
		if (parameter == SIDScoreIR.EffectParameter.PITCH) {
			if (ctx.NOTE() == null) {
				throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
						"PITCH sweep expects note values");
			}
			return SIDScoreIR.EffectValueIR.pitch(parseEffectPitch(ctx.NOTE().getText(),
					ctx.NOTE().getSymbol(), "PITCH sweep"));
		}
		if (ctx.numericValue() == null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					parameter + " sweep expects numeric values");
		}
		return switch (parameter) {
		case FREQ -> parseEffectNumericValue(ctx.numericValue(), "FREQ sweep", 0, 0xFFFF);
		case PW -> parseEffectNumericValue(ctx.numericValue(), "PW sweep", 0, 0x0FFF);
		case CUTOFF -> parseEffectNumericValue(ctx.numericValue(), "CUTOFF sweep", 0, 0x07FF);
		case VOLUME -> parseEffectNumericValue(ctx.numericValue(), "VOLUME sweep", 0, 15);
		default -> throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
				"Invalid sweep parameter " + parameter);
		};
	}

	private static SIDScoreIR.EffectSweepCurve parseEffectSweepCurve(SIDScoreParser.EffectSweepCurveContext ctx) {
		if (ctx == null)
			return SIDScoreIR.EffectSweepCurve.LINEAR;
		if (ctx.EXP() != null)
			return SIDScoreIR.EffectSweepCurve.EXP;
		if (ctx.LOG() != null)
			return SIDScoreIR.EffectSweepCurve.LOG;
		if (ctx.STEP() != null)
			return SIDScoreIR.EffectSweepCurve.STEP;
		return SIDScoreIR.EffectSweepCurve.LINEAR;
	}

	private static SIDScoreIR.EffectRetriggerMode parseEffectRetriggerMode(
			SIDScoreParser.EffectRetriggerModeContext ctx) {
		if (ctx.IGNORE() != null)
			return SIDScoreIR.EffectRetriggerMode.IGNORE;
		if (ctx.STEAL() != null)
			return SIDScoreIR.EffectRetriggerMode.STEAL;
		return SIDScoreIR.EffectRetriggerMode.RESTART;
	}

	private static void validateEffectPitchFreqConflicts(String effectName, List<SIDScoreIR.EffectStepIR> steps,
			Token where) {
		Map<Integer, EnumSet<SIDScoreIR.EffectParameter>> byTick = new LinkedHashMap<>();
		for (SIDScoreIR.EffectStepIR step : steps) {
			if (!(step instanceof SIDScoreIR.EffectAssignmentIR assignment))
				continue;
			SIDScoreIR.EffectParameter parameter = assignment.parameter();
			if (parameter != SIDScoreIR.EffectParameter.PITCH && parameter != SIDScoreIR.EffectParameter.FREQ)
				continue;
			EnumSet<SIDScoreIR.EffectParameter> params = byTick.computeIfAbsent(assignment.tick(),
					ignored -> EnumSet.noneOf(SIDScoreIR.EffectParameter.class));
			SIDScoreIR.EffectParameter other = parameter == SIDScoreIR.EffectParameter.PITCH
					? SIDScoreIR.EffectParameter.FREQ
					: SIDScoreIR.EffectParameter.PITCH;
			if (params.contains(other)) {
				throw new ValidationException(posLine(where), posCol(where),
						"EFFECT " + effectName + " sets PITCH and FREQ at tick " + assignment.tick());
			}
			params.add(parameter);
		}
	}

	// ---------------------------
	// Internal voice build state
	// ---------------------------
	private static final class SongBuildState {
		final int number;
		Optional<String> title = Optional.empty();
		Optional<String> author = Optional.empty();
		Optional<String> released = Optional.empty();
		OptionalInt tempoBpm = OptionalInt.empty();
		Optional<SIDScoreIR.TimeSigIR> timeSig = Optional.empty();
		Optional<SIDScoreIR.VideoSystem> system = Optional.empty();
		Optional<SIDScoreIR.SwingSetting> defaultSwing = Optional.empty();
		final Map<String, SIDScoreIR.EffectIR> effects = new LinkedHashMap<>();
		final Map<Integer, SIDScoreIR.VoiceIR> voices = new LinkedHashMap<>();

		SongBuildState(int number) {
			this.number = number;
		}
	}

	private static final class VoiceBuildState {
		final int voiceIndex;
		final String instrumentName;

		final List<SIDScoreIR.VoiceItemIR> rootItems = new ArrayList<>();
		final Deque<ScopeFrame> scopeStack = new ArrayDeque<>();

		int octave = 4;

		VoiceBuildState(int voiceIndex, String instrumentName) {
			if (voiceIndex < 1 || voiceIndex > 3)
				throw new IllegalArgumentException("VOICE index must be 1..3");
			this.voiceIndex = voiceIndex;
			this.instrumentName = instrumentName;
		}

		void emit(SIDScoreIR.VoiceItemIR item) {
			if (scopeStack.isEmpty())
				rootItems.add(item);
			else
				scopeStack.peek().items.add(item);
		}

		void pushScope(ScopeKind kind) {
			scopeStack.push(new ScopeFrame(kind));
		}

		List<SIDScoreIR.VoiceItemIR> popScope(ScopeKind expected, Token where) {
			if (scopeStack.isEmpty())
				throw new ValidationException(posLine(where), posCol(where), "Scope underflow: expected " + expected);
			ScopeFrame f = scopeStack.pop();
			if (f.kind != expected)
				throw new ValidationException(posLine(where), posCol(where),
						"Scope mismatch: expected " + expected + " got " + f.kind);
			return List.copyOf(f.items);
		}

		void assertScopesClosed(Token where) {
			if (!scopeStack.isEmpty()) {
				throw new ValidationException(posLine(where), posCol(where),
						"Unclosed scope in VOICE " + voiceIndex + ": " + scopeStack.peek().kind);
			}
		}

		void setOctave(int o) {
			octave = clamp(o);
		}

		void octaveUp() {
			octave = clamp(octave + 1);
		}

		void octaveDown() {
			octave = clamp(octave - 1);
		}

		private static int clamp(int o) {
			return Math.max(0, Math.min(8, o));
		}

		private static final class ScopeFrame {
			final ScopeKind kind;
			final List<SIDScoreIR.VoiceItemIR> items = new ArrayList<>();

			ScopeFrame(ScopeKind kind) {
				this.kind = kind;
			}
		}
	}

	// ---------------------------
	// Token parsing helpers
	// ---------------------------
	private static SIDScoreIR.SwingSetting parseSwingSetting(SIDScoreParser.SwingStmtContext ctx) {
		if (ctx.OFF() != null)
			return new SIDScoreIR.SwingOff();
		int pct = Integer.parseInt(ctx.INT().getText());
		return new SIDScoreIR.SwingPercent(pct);
	}

	private static int waveMaskFromWaveList(SIDScoreParser.WaveListContext ctx) {
		int mask = 0;
		for (var w : ctx.WAVEVAL()) {
			mask |= SIDScoreIR.Wave.valueOf(w.getText()).mask;
		}
		return mask;
	}

	private static int filterMaskFromFilterSpec(SIDScoreParser.FilterSpecContext spec) {
		if (spec.OFF() != null)
			return 0;
		int mask = 0;
		for (SIDScoreParser.FilterModeContext fm : spec.filterList().filterMode()) {
			if (fm.LP() != null) {
				mask |= SIDScoreIR.FilterMode.LP.mask;
			} else if (fm.BP() != null) {
				mask |= SIDScoreIR.FilterMode.BP.mask;
			} else if (fm.HP() != null) {
				mask |= SIDScoreIR.FilterMode.HP.mask;
			}
		}
		return mask;
	}

	private static SIDScoreIR.EffectValueIR parseEffectOnOff(SIDScoreParser.OnOffContext ctx) {
		return SIDScoreIR.EffectValueIR.onOff(ctx.ON() != null);
	}

	private static SIDScoreIR.EffectValueIR parseEffectNumericValue(SIDScoreParser.NumericValueContext ctx,
			String label, int min, int max) {
		int value;
		if (ctx.HEX() != null) {
			value = parseHexValue(ctx.HEX().getText(), ctx.HEX().getSymbol(), label);
		} else {
			value = parseEffectInt(ctx.INT().getText(), ctx.INT().getSymbol(), label, min, max);
		}
		if (value < min || value > max) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					label + " out of range " + min + ".." + max);
		}
		return SIDScoreIR.EffectValueIR.integer(value);
	}

	private static SIDScoreIR.EffectValueIR parseEffectIntValue(TerminalNode node, String label, int min, int max) {
		return SIDScoreIR.EffectValueIR.integer(parseEffectInt(node.getText(), node.getSymbol(), label, min, max));
	}

	private static final Pattern NOTE_PAT = Pattern.compile("^(?<letter>[A-G])(?<acc>[#b])?(?<len>\\d+)?(?<dot>\\.)?$");

	private static SIDScoreIR.NoteIR parseNoteToken(String text, int currentOctave) {
		Matcher m = NOTE_PAT.matcher(text);
		if (!m.matches())
			throw new IllegalStateException("Bad NOTE token: " + text);

		SIDScoreIR.NoteLetter letter = SIDScoreIR.NoteLetter.valueOf(m.group("letter"));
		SIDScoreIR.Accidental acc = SIDScoreIR.Accidental.NATURAL;

		String accStr = m.group("acc");
		if ("#".equals(accStr))
			acc = SIDScoreIR.Accidental.SHARP;
		else if ("b".equals(accStr))
			acc = SIDScoreIR.Accidental.FLAT;

		Optional<SIDScoreIR.LengthIR> len = Optional.empty();
		String lenStr = m.group("len");
		if (lenStr != null)
			len = Optional.of(SIDScoreIR.LengthIR.fromDenom(Integer.parseInt(lenStr)));

		boolean dotted = m.group("dot") != null;

		SIDScoreIR.PitchIR pitch = new SIDScoreIR.PitchIR(letter, acc, currentOctave);
		return new SIDScoreIR.NoteIR(pitch, len, dotted);
	}

	private static int parsePitchToken(String text, Token where) {
		Matcher m = NOTE_PAT.matcher(text);
		if (!m.matches())
			throw new ValidationException(posLine(where), posCol(where), "NOTE= expects a pitch like C4 or F#5");
		if (m.group("dot") != null) {
			throw new ValidationException(posLine(where), posCol(where), "NOTE= pitch cannot be dotted");
		}
		String octStr = m.group("len");
		if (octStr == null) {
			throw new ValidationException(posLine(where), posCol(where), "NOTE= pitch must include an octave (e.g., C4)");
		}
		int octave = Integer.parseInt(octStr);

		SIDScoreIR.NoteLetter letter = SIDScoreIR.NoteLetter.valueOf(m.group("letter"));
		SIDScoreIR.Accidental acc = SIDScoreIR.Accidental.NATURAL;
		String accStr = m.group("acc");
		if ("#".equals(accStr))
			acc = SIDScoreIR.Accidental.SHARP;
		else if ("b".equals(accStr))
			acc = SIDScoreIR.Accidental.FLAT;

		SIDScoreIR.PitchIR pitch = new SIDScoreIR.PitchIR(letter, acc, octave);
		return pitch.toMidi();
	}

	private static int parseEffectPitch(String text, Token where, String label) {
		Matcher m = NOTE_PAT.matcher(text);
		if (!m.matches() || m.group("dot") != null || m.group("len") == null) {
			throw new ValidationException(posLine(where), posCol(where),
					label + " expects a pitch like C4 or F#5");
		}

		SIDScoreIR.NoteLetter letter = SIDScoreIR.NoteLetter.valueOf(m.group("letter"));
		SIDScoreIR.Accidental acc = SIDScoreIR.Accidental.NATURAL;
		String accStr = m.group("acc");
		if ("#".equals(accStr))
			acc = SIDScoreIR.Accidental.SHARP;
		else if ("b".equals(accStr))
			acc = SIDScoreIR.Accidental.FLAT;

		int octave = Integer.parseInt(m.group("len"));
		int midi = new SIDScoreIR.PitchIR(letter, acc, octave).toMidi();
		if (midi < 0 || midi > 127) {
			throw new ValidationException(posLine(where), posCol(where),
					label + " out of MIDI range 0..127");
		}
		return midi;
	}

	private static final Pattern REST_PAT = Pattern.compile("^R(?<len>\\d+)?(?<dot>\\.)?$");

	private static SIDScoreIR.RestIR parseRestToken(String text) {
		Matcher m = REST_PAT.matcher(text);
		if (!m.matches())
			throw new IllegalStateException("Bad REST token: " + text);

		Optional<SIDScoreIR.LengthIR> len = Optional.empty();
		String lenStr = m.group("len");
		if (lenStr != null)
			len = Optional.of(SIDScoreIR.LengthIR.fromDenom(Integer.parseInt(lenStr)));

		boolean dotted = m.group("dot") != null;
		return new SIDScoreIR.RestIR(len, dotted);
	}

	private static final Pattern HIT_PAT = Pattern.compile("^X(?<len>\\d+)?(?<dot>\\.)?$");

	private static SIDScoreIR.HitIR parseHitToken(String text) {
		Matcher m = HIT_PAT.matcher(text);
		if (!m.matches())
			throw new IllegalStateException("Bad HIT token: " + text);

		Optional<SIDScoreIR.LengthIR> len = Optional.empty();
		String lenStr = m.group("len");
		if (lenStr != null)
			len = Optional.of(SIDScoreIR.LengthIR.fromDenom(Integer.parseInt(lenStr)));

		boolean dotted = m.group("dot") != null;
		return new SIDScoreIR.HitIR(len, dotted);
	}

	private static int parsePrefixedInt(String text, char prefix) {
		if (text.length() < 2 || text.charAt(0) != prefix)
			throw new IllegalArgumentException("Expected " + prefix + "<n>, got: " + text);
		return Integer.parseInt(text.substring(1));
	}

	private static int parseNonNegativeInt(String text, Token where, String label) {
		return parseEffectInt(text, where, label, 0, Integer.MAX_VALUE);
	}

	private static int parseEffectInt(String text, Token where, String label, int min, int max) {
		int value;
		try {
			value = Integer.parseInt(text);
		} catch (NumberFormatException ex) {
			throw new ValidationException(posLine(where), posCol(where), label + " must be an integer");
		}
		if (value < min || value > max) {
			throw new ValidationException(posLine(where), posCol(where),
					label + " out of range " + min + ".." + max);
		}
		return value;
	}

	private static int parseHexValue(String raw, Token where, String label) {
		int v;
		if (raw == null || raw.isEmpty() || raw.charAt(0) != '$') {
			throw new ValidationException(posLine(where), posCol(where), label + " must be $-prefixed hex");
		}
		try {
			v = Integer.parseInt(raw.substring(1), 16);
		} catch (NumberFormatException e) {
			throw new ValidationException(posLine(where), posCol(where), label + " must be $-prefixed hex");
		}
		if (v < 0 || v > 0xFFFF) {
			throw new ValidationException(posLine(where), posCol(where), label + " out of range $0000..$FFFF");
		}
		return v;
	}

	private static int parseSignedInt(SIDScoreParser.SignedIntContext ctx) {
		int v = Integer.parseInt(ctx.INT().getText());
		if (ctx.MINUS() != null)
			v = -v;
		return v;
	}

	private static String unquote(String s) {
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\""))
			return s.substring(1, s.length() - 1);
		return s;
	}

	private static int posLine(Token t) {
		return t.getLine();
	}

	private static int posCol(Token t) {
		return t.getCharPositionInLine();
	}
}
