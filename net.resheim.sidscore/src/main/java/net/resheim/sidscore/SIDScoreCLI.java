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
 *   java SidScoreMain demo.sidscore
 */ 
public final class SIDScoreCLI {

  private static final String USAGE = "Usage: java SidScoreMain <file.sidscore> [--wav <out.wav>] [--asm <out.asm>] "
      + "[--prg <out.prg>] [--sid <out.sid>] [--sid-model <6581|8580>] [--sid-waveforms <path>] [--no-play]";

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println(USAGE);
      System.exit(2);
    }

    Path wavOut = null;
    Path asmOut = null;
    Path prgOut = null;
    Path sidOut = null;
    Path sidWaveforms = null;
    SidModel sidModel = SidModel.MOS6581;
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

    if (asmOut != null || prgOut != null || sidOut != null) {
      SIDScoreExporter exporter = new SIDScoreExporter();

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
        exporter.writeAsm(timed, asmForPrg, true);
        System.out.println("ASM: " + asmForPrg);
      }
      if (asmForSid != null && (asmForPrg == null || !asmForSid.equals(asmForPrg))) {
        deleteIfExists(asmForSid);
        exporter.writeAsm(timed, asmForSid, false);
        System.out.println("ASM (SID): " + asmForSid);
      }

      Path prgForPrg = null;
      if (prgOut != null) {
        prgForPrg = prgOut;
        if (asmForPrg == null) {
          asmForPrg = withExtension(prgForPrg, ".asm");
          deleteIfExists(asmForPrg);
          exporter.writeAsm(timed, asmForPrg, true);
          System.out.println("ASM: " + asmForPrg);
        }
        deleteIfExists(prgForPrg);
        exporter.assemble(asmForPrg, prgForPrg);
        System.out.println("PRG: " + prgForPrg);
      }

      if (sidOut != null) {
        Path prgForSid = (prgForPrg != null && asmForSid != null && asmForSid.equals(asmForPrg))
            ? prgForPrg
            : withExtension(sidOut, ".prg");
        if (asmForSid == null) {
          asmForSid = withExtension(sidOut, ".asm");
          deleteIfExists(asmForSid);
          exporter.writeAsm(timed, asmForSid, false);
          System.out.println("ASM (SID): " + asmForSid);
        }
        if (prgForPrg == null || !prgForSid.equals(prgForPrg)) {
          deleteIfExists(prgForSid);
          exporter.assemble(asmForSid, prgForSid);
          System.out.println("PRG (SID): " + prgForSid);
        }
        deleteIfExists(sidOut);
        exporter.writeSid(prgForSid, timed, sidOut, sidModel);
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
