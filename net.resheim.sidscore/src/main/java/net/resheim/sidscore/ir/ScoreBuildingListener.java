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
	private final Map<Integer, SIDScoreIR.VoiceIR> voices = new LinkedHashMap<>();

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
		for (var e : voices.entrySet()) {
			int vIdx = e.getKey();
			SIDScoreIR.VoiceIR v = e.getValue();
			SIDScoreIR.InstrumentIR instr = instruments.get(v.instrumentName());
			if (instr == null) {
				// This is a semantic error; location is VOICE header, but we didn't store it
				// here.
				// Fail fast anyway.
				throw new IllegalStateException(
						"VOICE " + vIdx + " references undefined instrument: " + v.instrumentName());
			}
			boolean isNoise = instr.hasWave(SIDScoreIR.Wave.NOISE);
			if (!isNoise && containsHit(v.items())) {
				throw new IllegalStateException(
						"VOICE " + vIdx + " uses X but instrument is not NOISE: " + instr.name());
			}
		}

		return new SIDScoreIR.ScoreIR(title, author, released, tempoBpm, timeSig, system, defaultSwing,
				Collections.unmodifiableMap(tables), Collections.unmodifiableMap(instruments),
				Collections.unmodifiableMap(voices));
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
		title = Optional.of(unquote(ctx.STRING().getText()));
	}

	@Override
	public void exitAuthorStmt(SIDScoreParser.AuthorStmtContext ctx) {
		if (importMode)
			return;
		author = Optional.of(unquote(ctx.STRING().getText()));
	}

	@Override
	public void exitReleasedStmt(SIDScoreParser.ReleasedStmtContext ctx) {
		if (importMode)
			return;
		released = Optional.of(unquote(ctx.STRING().getText()));
	}

	@Override
	public void exitTempoStmt(SIDScoreParser.TempoStmtContext ctx) {
		if (importMode)
			return;
		tempoBpm = Integer.parseInt(ctx.INT().getText());
	}

	@Override
	public void exitTimeStmt(SIDScoreParser.TimeStmtContext ctx) {
		if (importMode)
			return;
		int num = Integer.parseInt(ctx.INT(0).getText());
		int den = Integer.parseInt(ctx.INT(1).getText());
		timeSig = Optional.of(new SIDScoreIR.TimeSigIR(num, den));
	}

	@Override
	public void exitSystemStmt(SIDScoreParser.SystemStmtContext ctx) {
		if (importMode)
			return;
		if (system.isPresent()) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"SYSTEM already specified");
		}
		if (ctx.PAL() != null) {
			system = Optional.of(SIDScoreIR.VideoSystem.PAL);
		} else if (ctx.NTSC() != null) {
			system = Optional.of(SIDScoreIR.VideoSystem.NTSC);
		}
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
	public void exitSwingStmt(SIDScoreParser.SwingStmtContext ctx) {
		if (importMode)
			return;
		SIDScoreIR.SwingSetting s = parseSwingSetting(ctx);
		if (currentVoice == null)
			defaultSwing = s;
		else
			currentVoice.emit(new SIDScoreIR.SetSwingIR(s));
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
		if (voices.putIfAbsent(v.voiceIndex, voiceIR) != null) {
			throw new ValidationException(posLine(ctx.getStart()), posCol(ctx.getStart()),
					"Duplicate VOICE " + v.voiceIndex);
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
	// Internal voice build state
	// ---------------------------
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
