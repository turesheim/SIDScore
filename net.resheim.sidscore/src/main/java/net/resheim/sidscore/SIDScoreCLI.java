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
package net.resheim.sidscore;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.resheim.sidscore.export.SIDScoreExporter;
import net.resheim.sidscore.export.driver.SidDriverBackend;
import net.resheim.sidscore.export.driver.SidDriverRegistry;
import net.resheim.sidscore.ir.RealtimeAudioPlayer;
import net.resheim.sidscore.ir.SIDScoreIR;
import net.resheim.sidscore.ir.ScoreBuildingListener;
import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;
import net.resheim.sidscore.sid.SidModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Minimal test-main:
 * - Reads a .sidscore file
 * - Parses with ANTLR
 * - Builds ScoreIR via ScoreBuildingListener
 * - Resolves to TimedScore (ticks + gate modes)
 * - Prints a short summary + first events per voice
 *
 * Usage:
 *   java SIDScoreCLI demo.sidscore
 */ 
public final class SIDScoreCLI {

  private static final String DEFAULT_DRIVER = "sidscore";
  private static final String USAGE = "Usage: java SIDScoreCLI <file.sidscore> [--wav <out.wav>] [--asm <out.asm>] "
      + "[--prg <out.prg>] [--sid <out.sid>] [--driver <id>] [--list-drivers] "
      + "[--sid-model <6581|8580>] [--sid-waveforms <path>] [--no-play]";

  public static void main(String[] args) throws Exception {
    SidDriverRegistry driverRegistry = SidDriverRegistry.load();

    if (args.length == 1 && "--list-drivers".equals(args[0])) {
      printDrivers(driverRegistry);
      return;
    }
    if (args.length < 1 || args[0].startsWith("--")) {
      System.err.println(USAGE);
      System.exit(2);
    }

    Path wavOut = null;
    Path asmOut = null;
    Path prgOut = null;
    Path sidOut = null;
    Path sidWaveforms = null;
    SidModel sidModel = SidModel.MOS6581;
    String driverId = DEFAULT_DRIVER;
    boolean noPlay = false;
    for (int i = 1; i < args.length; i++) {
      switch (args[i]) {
        case "--wav" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          wavOut = Path.of(args[++i]);
        }
        case "--asm" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          asmOut = Path.of(args[++i]);
        }
        case "--prg" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          prgOut = Path.of(args[++i]);
        }
        case "--sid" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          sidOut = Path.of(args[++i]);
        }
        case "--sid-model" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          try {
            sidModel = SidModel.parse(args[++i]);
          } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
            System.exit(2);
          }
        }
        case "--sid-waveforms" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          sidWaveforms = Path.of(args[++i]);
        }
        case "--driver" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          driverId = args[++i];
        }
        case "--list-drivers" -> {
          printDrivers(driverRegistry);
          return;
        }
        case "--no-play" -> noPlay = true;
        default -> {
          System.err.println(USAGE);
          System.exit(2);
        }
      }
    }

    if (noPlay && wavOut == null && asmOut == null && prgOut == null && sidOut == null) {
      System.err.println("--no-play requires an output option (e.g. --wav, --asm, --prg, --sid)");
      System.exit(2);
    }

    String src = Files.readString(Path.of(args[0]));

    // --- ANTLR parse ---
	SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(src));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
	SIDScoreParser parser = new SIDScoreParser(tokens);

    // Basic error handling (fail fast)
    parser.removeErrorListeners();
    parser.addErrorListener(new ThrowingErrorListener());

    ParseTree tree = parser.file();

    // --- Build ScoreIR ---
    ScoreBuildingListener builder = new ScoreBuildingListener(Path.of(args[0]));
    ParseTreeWalker.DEFAULT.walk(builder, tree);

    SIDScoreIR.ScoreIR scoreIR = builder.buildScoreIR();

    // --- Resolve to TimedScore ---
    SIDScoreIR.Resolver.Result result = new SIDScoreIR.Resolver().resolve(scoreIR);

    // --- Print diagnostics (warnings) ---
    var diags = result.diagnostics().messages();
    var warnings = diags.stream()
        .filter(m -> m.severity() == SIDScoreIR.Diagnostics.Severity.WARNING)
        .collect(Collectors.toList());
    if (!warnings.isEmpty()) {
      System.out.println("Warnings:");
      for (var w : warnings) System.out.println("  - " + w.text());
      System.out.println();
    }

    // --- Print summary ---
    SIDScoreIR.TimedScore timed = result.timedScore();
    SidDriverBackend driver = driverRegistry.find(driverId).orElse(null);
    if (driver == null) {
      System.err.println("Unknown driver backend: " + driverId);
      printDrivers(driverRegistry);
      System.exit(2);
      return;
    }

    if (asmOut != null || prgOut != null || sidOut != null) {
      SIDScoreExporter exporter = new SIDScoreExporter();
      System.out.println("Driver: " + driver.id() + " (" + driver.description() + ")");

      Path asmForPrg = null;
      Path asmForSid = null;

      if (sidOut != null) {
        asmForSid = asmOut != null ? asmOut : withExtension(sidOut, ".asm");
      }
      if (prgOut != null) {
        if (asmOut != null && sidOut != null) {
          asmForPrg = withExtension(prgOut, ".asm");
        } else {
          asmForPrg = asmOut != null ? asmOut : withExtension(prgOut, ".asm");
        }
      }
      if (asmOut != null && prgOut == null && sidOut == null) {
        asmForPrg = asmOut;
      }

      if (asmForPrg != null) {
        deleteIfExists(asmForPrg);
        driver.writeAsm(timed, asmForPrg, true);
        System.out.println("ASM: " + asmForPrg);
      }
      if (asmForSid != null && (asmForPrg == null || !asmForSid.equals(asmForPrg))) {
        deleteIfExists(asmForSid);
        driver.writeAsm(timed, asmForSid, false);
        System.out.println("ASM (SID): " + asmForSid);
      }

      Path prgForPrg = null;
      if (prgOut != null) {
        prgForPrg = prgOut;
        if (asmForPrg == null) {
          asmForPrg = withExtension(prgForPrg, ".asm");
          deleteIfExists(asmForPrg);
          driver.writeAsm(timed, asmForPrg, true);
          System.out.println("ASM: " + asmForPrg);
        }
        deleteIfExists(prgForPrg);
        exporter.assemble(asmForPrg, prgForPrg);
        System.out.println("PRG: " + prgForPrg);
      }

      if (sidOut != null) {
        if (!driver.supportsSidExport()) {
          throw new IllegalStateException("Driver backend does not support SID export: " + driver.id());
        }
        Path prgForSid = (prgForPrg != null && asmForSid != null && asmForSid.equals(asmForPrg))
            ? prgForPrg
            : withExtension(sidOut, ".prg");
        if (asmForSid == null) {
          asmForSid = withExtension(sidOut, ".asm");
          deleteIfExists(asmForSid);
          driver.writeAsm(timed, asmForSid, false);
          System.out.println("ASM (SID): " + asmForSid);
        }
        if (prgForPrg == null || !prgForSid.equals(prgForPrg)) {
          deleteIfExists(prgForSid);
          exporter.assemble(asmForSid, prgForSid);
          System.out.println("PRG (SID): " + prgForSid);
        }
        deleteIfExists(sidOut);
        exporter.writeSid(prgForSid, timed, sidOut, sidModel, driver.psidAddresses());
        System.out.println("SID: " + sidOut);
      }
      System.out.println();
    }

	// Play music (and optionally capture WAV)
	if (noPlay) {
		if (wavOut != null) {
			deleteIfExists(wavOut);
			new RealtimeAudioPlayer(sidModel, sidWaveforms).renderToWav(timed, wavOut);
			System.out.println("WAV: " + wavOut);
		}
	} else if (wavOut != null) {
		deleteIfExists(wavOut);
		new RealtimeAudioPlayer(sidModel, sidWaveforms).play(timed, wavOut);
		System.out.println("WAV: " + wavOut);
	} else {
		new RealtimeAudioPlayer(sidModel, sidWaveforms).play(timed);
	}

    System.out.println("Title: " + timed.title().orElse("(none)"));
    System.out.println("Author: " + timed.author().orElse("(none)"));
    System.out.println("Released: " + timed.released().orElse("(none)"));
    System.out.println("Tempo: " + timed.tempoBpm() + " BPM");
    System.out.println("Ticks: " + timed.ticksPerWhole() + " per whole note");
    System.out.println("System: " + timed.system());
    System.out.println("Voices: " + timed.voices().size());
    System.out.println();

    // --- Voice summaries ---
    for (int v = 1; v <= 3; v++) {
      SIDScoreIR.TimedVoice tv = timed.voices().get(v);
      if (tv == null) continue;
      int events = tv.events().size();
      int ticks = tv.events().stream().mapToInt(SIDScoreIR.TimedEvent::durationTicks).sum();
      System.out.println("VOICE " + v + ": events=" + events + " ticks=" + ticks);
    }
  }

  private static Path withExtension(Path path, String ext) {
    String name = path.getFileName().toString();
    int dot = name.lastIndexOf('.');
    String base = dot >= 0 ? name.substring(0, dot) : name;
    String newName = base + ext;
    Path parent = path.getParent();
    return parent == null ? Path.of(newName) : parent.resolve(newName);
  }

  private static void deleteIfExists(Path path) throws Exception {
    if (path == null) return;
    if (Files.exists(path)) {
      if (Files.isDirectory(path)) {
        throw new IllegalStateException("Output path is a directory: " + path);
      }
      Files.delete(path);
    }
  }

  private static void printDrivers(SidDriverRegistry registry) {
    System.out.println("Available driver backends:");
    for (SidDriverBackend backend : registry.list()) {
      System.out.println("  - " + backend.id() + ": " + backend.description());
    }
  }

  /** ErrorListener that throws immediately on syntax errors. */
  static final class ThrowingErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e) {
      throw new RuntimeException("Syntax error at " + line + ":" + charPositionInLine + " - " + msg, e);
    }
  }
}
