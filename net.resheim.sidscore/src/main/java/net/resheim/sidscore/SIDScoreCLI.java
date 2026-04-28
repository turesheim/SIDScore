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
import net.resheim.sidscore.midi.MidiInputRouter;
import net.resheim.sidscore.parser.SIDScoreLexer;
import net.resheim.sidscore.parser.SIDScoreParser;
import net.resheim.sidscore.sid.SidModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
  private static final record ParsedScore(SIDScoreIR.ScoreIR scoreIR, SIDScoreIR.Resolver.Result resolved) {}

  private static final String DEFAULT_DRIVER = "sidscore";
  private static final String USAGE = "Usage: java SIDScoreCLI <file.sidscore> [--stitch <more.sidscore>]... "
      + "[--wav <out.wav>] [--asm <out.asm>] [--prg <out.prg>] [--sid <out.sid>] [--driver <id>] [--list-drivers] "
      + "[--sid-model <6581|8580>] [--sid-waveforms <path>] [--midi] [--midi-device <index|name>] "
      + "[--midi-map <voice:channel,...>] [--list-midi-devices] [--no-play]\n"
      + "       java SIDScoreCLI --player-server [--port <port>]";

  public static void main(String[] args) throws Exception {
    if (args.length > 0 && "--player-server".equals(args[0])) {
      String[] serverArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
      SIDScorePlayerServer.main(serverArgs);
      return;
    }

    SidDriverRegistry driverRegistry = SidDriverRegistry.load();

    if (args.length == 1 && "--list-drivers".equals(args[0])) {
      printDrivers(driverRegistry);
      return;
    }
    if (args.length == 1 && "--list-midi-devices".equals(args[0])) {
      printMidiDevices();
      return;
    }
    if (args.length < 1 || args[0].startsWith("--")) {
      System.err.println(USAGE);
      System.exit(2);
    }

    Path sourcePath = Path.of(args[0]);
    Path wavOut = null;
    Path asmOut = null;
    Path prgOut = null;
    Path sidOut = null;
    Path sidWaveforms = null;
    SidModel sidModel = SidModel.MOS6581;
    String driverId = DEFAULT_DRIVER;
    boolean noPlay = false;
    boolean midiEnabled = false;
    String midiDeviceSelector = null;
    Map<Integer, Integer> midiVoiceMap = MidiInputRouter.defaultVoiceChannelMap();
    List<Path> stitchInputs = new ArrayList<>();
    for (int i = 1; i < args.length; i++) {
      switch (args[i]) {
        case "--stitch" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          stitchInputs.add(Path.of(args[++i]));
        }
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
        case "--list-midi-devices" -> {
          printMidiDevices();
          return;
        }
        case "--midi" -> midiEnabled = true;
        case "--midi-device" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          midiDeviceSelector = args[++i];
          midiEnabled = true;
        }
        case "--midi-map" -> {
          if (i + 1 >= args.length) {
            System.err.println(USAGE);
            System.exit(2);
          }
          try {
            midiVoiceMap = MidiInputRouter.parseVoiceChannelMap(args[++i]);
          } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(USAGE);
            System.exit(2);
          }
          midiEnabled = true;
        }
        case "--no-play" -> noPlay = true;
        default -> {
          System.err.println(USAGE);
          System.exit(2);
        }
      }
    }

    if (!stitchInputs.isEmpty()) {
      if (sidOut == null) {
        System.err.println("--stitch requires --sid <out.sid>");
        System.exit(2);
      }
      if (!noPlay) {
        System.err.println("--stitch requires --no-play");
        System.exit(2);
      }
      if (asmOut != null || prgOut != null || wavOut != null) {
        System.err.println("--stitch is only supported for SID export");
        System.exit(2);
      }
    }

    if (midiEnabled && noPlay) {
      System.err.println("--midi requires realtime playback and cannot be used with --no-play");
      System.exit(2);
    }
    if (midiEnabled && wavOut != null) {
      System.err.println("--midi cannot be combined with --wav recording");
      System.exit(2);
    }

    if (noPlay && wavOut == null && asmOut == null && prgOut == null && sidOut == null) {
      System.err.println("--no-play requires an output option (e.g. --wav, --asm, --prg, --sid)");
      System.exit(2);
    }

    ParsedScore parsed = parseResolved(sourcePath);
    printWarnings(parsed.resolved(), sourcePath.toString());
    SIDScoreIR.ScoreIR scoreIR = parsed.scoreIR();
    SIDScoreIR.TimedScore timed = parsed.resolved().timedScore();
    SidDriverBackend driver = driverRegistry.find(driverId).orElse(null);
    if (driver == null) {
      System.err.println("Unknown driver backend: " + driverId);
      printDrivers(driverRegistry);
      System.exit(2);
      return;
    }
    List<String> sidBundleSources = new ArrayList<>();
    List<SIDScoreIR.TimedScore> sidBundleTunes = new ArrayList<>();
    if (sidOut != null && (!scoreIR.subtunes().isEmpty() || !scoreIR.songs().isEmpty() || !stitchInputs.isEmpty())) {
      Map<Integer, SIDScoreIR.TimedScore> inlineSongs = new TreeMap<>();
      for (var entry : scoreIR.songs().entrySet()) {
        int number = entry.getKey();
        if (number <= 1) {
          throw new IllegalStateException("TUNE number must be >= 2, got " + number);
        }
        SIDScoreIR.ScoreIR inlineScoreIR = buildInlineSongScore(scoreIR, entry.getValue());
        SIDScoreIR.Resolver.Result inlineResult = new SIDScoreIR.Resolver().resolve(inlineScoreIR);
        printWarnings(inlineResult, sourcePath + " [TUNE " + number + "]");
        inlineSongs.put(number, inlineResult.timedScore());
      }

      Map<Integer, Path> externalSubtunes = new TreeMap<>();
      for (var entry : scoreIR.subtunes().entrySet()) {
        int number = entry.getKey();
        if (number <= 1) {
          throw new IllegalStateException("IMPORT AS number must be >= 2, got " + number);
        }
        if (inlineSongs.containsKey(number)) {
          throw new IllegalStateException("Duplicate subtune number " + number
              + " in both TUNE and IMPORT definitions");
        }
        externalSubtunes.put(number, entry.getValue().toAbsolutePath().normalize());
      }
      int nextImplicitSong = maxSongNumber(inlineSongs, externalSubtunes) + 1;
      for (Path stitchPath : stitchInputs) {
        while (inlineSongs.containsKey(nextImplicitSong) || externalSubtunes.containsKey(nextImplicitSong)) {
          nextImplicitSong++;
        }
        externalSubtunes.put(nextImplicitSong, stitchPath.toAbsolutePath().normalize());
        nextImplicitSong++;
      }
      validateContiguousSongs(inlineSongs, externalSubtunes);

      if (!inlineSongs.isEmpty() || !externalSubtunes.isEmpty()) {
        sidBundleSources.add(sourcePath.toString());
        sidBundleTunes.add(timed);
        int maxSong = Math.max(
            inlineSongs.isEmpty() ? 1 : inlineSongs.keySet().stream().max(Integer::compareTo).orElse(1),
            externalSubtunes.isEmpty() ? 1 : externalSubtunes.keySet().stream().max(Integer::compareTo).orElse(1));
        for (int song = 2; song <= maxSong; song++) {
          SIDScoreIR.TimedScore inlineTimed = inlineSongs.get(song);
          if (inlineTimed != null) {
            sidBundleSources.add(sourcePath + " [TUNE " + song + "]");
            sidBundleTunes.add(inlineTimed);
            continue;
          }
          Path tunePath = externalSubtunes.get(song);
          if (tunePath == null) {
            throw new IllegalStateException("Subtune numbers must be contiguous starting at 1 (missing tune " + song + ")");
          }
          ParsedScore extra = parseResolved(tunePath);
          printWarnings(extra.resolved(), tunePath.toString());
          sidBundleSources.add(tunePath.toString());
          sidBundleTunes.add(extra.resolved().timedScore());
        }
      }
    }

    if (asmOut != null || prgOut != null || sidOut != null) {
      SIDScoreExporter exporter = new SIDScoreExporter();
      System.out.println("Driver: " + driver.id() + " (" + driver.description() + ")");

      Path asmForPrg = null;
      Path asmForSid = null;
      Path compiledProgram = null;

      if (sidOut != null && sidBundleTunes.isEmpty()) {
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
        compiledProgram = prgForPrg;
      }

      if (sidOut != null) {
        if (!driver.supportsSidExport()) {
          throw new IllegalStateException("Driver backend does not support SID export: " + driver.id());
        }
        if (sidBundleTunes.isEmpty()) {
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
          compiledProgram = prgForSid;
          deleteIfExists(sidOut);
          exporter.writeSid(prgForSid, timed, sidOut, sidModel, driver.psidAddresses());
          System.out.println("SID: " + sidOut);
        } else {
          Path bundleDir = Files.createTempDirectory("sidscore-bundle-");
          List<Path> tunePrgs = new ArrayList<>();
          try {
            for (int i = 0; i < sidBundleTunes.size(); i++) {
              SIDScoreIR.TimedScore tune = sidBundleTunes.get(i);
              Path tuneAsm = bundleDir.resolve("tune-" + (i + 1) + ".asm");
              Path tunePrg = bundleDir.resolve("tune-" + (i + 1) + ".prg");
              driver.writeAsm(tune, tuneAsm, false);
              exporter.assemble(tuneAsm, tunePrg);
              tunePrgs.add(tunePrg);
              System.out.println("SID Tune " + (i + 1) + ": " + sidBundleSources.get(i));
            }
            deleteIfExists(sidOut);
            exporter.writeSidBundle(tunePrgs, sidBundleTunes, sidOut, sidModel, driver.psidAddresses());
            System.out.println("SID (bundle): " + sidOut + " tunes=" + sidBundleTunes.size());
            if (Files.exists(sidOut)) {
              System.out.println("SID Size: " + Files.size(sidOut) + " bytes");
            }
          } finally {
            deleteRecursively(bundleDir);
          }
        }
      }
      if (compiledProgram != null && Files.exists(compiledProgram)) {
        printProgramStats(driver, exporter, timed, compiledProgram, sidOut);
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
	} else if (midiEnabled) {
		RealtimeAudioPlayer player = new RealtimeAudioPlayer(sidModel, sidWaveforms);
		try (MidiInputRouter midi = MidiInputRouter.open(midiDeviceSelector, midiVoiceMap)) {
			Thread shutdownHook = new Thread(() -> {
				player.stop();
				midi.close();
			}, "sidscore-midi-shutdown");
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			try {
				System.out.println("MIDI Input: " + midi.deviceName());
				System.out.println("MIDI Map: " + formatMidiMap(midi.voiceChannelMap()));
				System.out.println("MIDI playback active. Press Ctrl+C to stop.");
				player.play(timed, midi);
			} finally {
				try {
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				} catch (IllegalStateException ignored) {
					// JVM shutdown already in progress.
				}
			}
		} catch (Exception e) {
			System.err.println("MIDI input failed: " + e.getMessage());
			System.err.println("Use --list-midi-devices to see available MIDI inputs.");
			System.exit(1);
		}
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

  private static ParsedScore parseResolved(Path sourcePath) throws Exception {
    String src = Files.readString(sourcePath);

    SIDScoreLexer lexer = new SIDScoreLexer(CharStreams.fromString(src));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    SIDScoreParser parser = new SIDScoreParser(tokens);

    parser.removeErrorListeners();
    parser.addErrorListener(new ThrowingErrorListener());

    ParseTree tree = parser.file();
    ScoreBuildingListener builder = new ScoreBuildingListener(sourcePath);
    ParseTreeWalker.DEFAULT.walk(builder, tree);

    SIDScoreIR.ScoreIR scoreIR = builder.buildScoreIR();
    SIDScoreIR.Resolver.Result resolved = new SIDScoreIR.Resolver().resolve(scoreIR);
    return new ParsedScore(scoreIR, resolved);
  }

  private static void printWarnings(SIDScoreIR.Resolver.Result result, String sourceLabel) {
    var warnings = result.diagnostics().messages().stream()
        .filter(m -> m.severity() == SIDScoreIR.Diagnostics.Severity.WARNING)
        .collect(Collectors.toList());
    if (warnings.isEmpty()) {
      return;
    }
    System.out.println("Warnings (" + sourceLabel + "):");
    for (var w : warnings) {
      System.out.println("  - " + w.text());
    }
    System.out.println();
  }

  private static int maxSongNumber(Map<Integer, SIDScoreIR.TimedScore> inlineSongs, Map<Integer, Path> externalSongs) {
    int inlineMax = inlineSongs.isEmpty() ? 1 : inlineSongs.keySet().stream().max(Integer::compareTo).orElse(1);
    int externalMax = externalSongs.isEmpty() ? 1 : externalSongs.keySet().stream().max(Integer::compareTo).orElse(1);
    return Math.max(inlineMax, externalMax);
  }

  private static void validateContiguousSongs(Map<Integer, SIDScoreIR.TimedScore> inlineSongs,
                                              Map<Integer, Path> externalSongs) {
    if (inlineSongs.isEmpty() && externalSongs.isEmpty()) {
      return;
    }
    int expected = 1;
    int max = maxSongNumber(inlineSongs, externalSongs);
    while (expected <= max) {
      if (expected != 1 && !inlineSongs.containsKey(expected) && !externalSongs.containsKey(expected)) {
        throw new IllegalStateException("Subtune numbers must be contiguous starting at 1 (missing tune " + expected + ")");
      }
      expected += 1;
    }
  }

  private static SIDScoreIR.ScoreIR buildInlineSongScore(SIDScoreIR.ScoreIR base, SIDScoreIR.SongIR song) {
    int tempo = song.tempoBpm().isPresent() ? song.tempoBpm().getAsInt() : base.tempoBpm();
    Map<String, SIDScoreIR.EffectIR> effects = new LinkedHashMap<>();
    if (!song.effects().isEmpty()) {
      effects.putAll(song.effects());
    } else {
      effects.putAll(base.effects());
      effects.putAll(song.effects());
    }
    return new SIDScoreIR.ScoreIR(
        song.title().isPresent() ? song.title() : base.title(),
        song.author().isPresent() ? song.author() : base.author(),
        song.released().isPresent() ? song.released() : base.released(),
        tempo,
        song.timeSig().isPresent() ? song.timeSig() : base.timeSig(),
        song.system().isPresent() ? song.system() : base.system(),
        song.defaultSwing().isPresent() ? song.defaultSwing().get() : base.defaultSwing(),
        base.tables(),
        base.instruments(),
        java.util.Collections.unmodifiableMap(effects),
        song.voices(),
        Map.of(),
        Map.of());
  }

  private static void deleteRecursively(Path path) throws Exception {
    if (path == null || !Files.exists(path)) {
      return;
    }
    try (var stream = Files.walk(path)) {
      var paths = stream
          .sorted((a, b) -> Integer.compare(b.getNameCount(), a.getNameCount()))
          .toList();
      for (Path p : paths) {
        Files.deleteIfExists(p);
      }
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

  private static void printMidiDevices() {
    List<MidiInputRouter.InputDevice> devices = MidiInputRouter.listInputDevices();
    if (devices.isEmpty()) {
      System.out.println("No MIDI input devices found.");
      return;
    }
    System.out.println("Available MIDI input devices:");
    for (MidiInputRouter.InputDevice device : devices) {
      System.out.println("  [" + device.index() + "] " + device.displayName()
          + " - " + device.description()
          + " (" + device.vendor() + " " + device.version() + ")");
    }
  }

  private static String formatMidiMap(Map<Integer, Integer> voiceChannelMap) {
    return voiceChannelMap.entrySet().stream()
        .map(e -> "voice " + e.getKey() + " <- channel " + e.getValue())
        .collect(Collectors.joining(", "));
  }

  private static void printProgramStats(SidDriverBackend driver,
                                        SIDScoreExporter exporter,
                                        SIDScoreIR.TimedScore timed,
                                        Path prgPath,
                                        Path sidPath) throws Exception {
    long imageBytes = prgImageBytes(prgPath);
    int loadAddress = (int) ((read16le(prgPath) & 0xFFFF));
    System.out.println("Compiled With Driver: " + driver.id());
    System.out.println("Program Size: " + imageBytes + " bytes (load $" + hex4(loadAddress) + ")");

    if ("sidscore".equalsIgnoreCase(driver.id())) {
      SIDScoreExporter.ProgramStats stats = exporter.estimateProgramStats(timed);
      long scoreBytes = stats.scoreBytes();
      long driverBytes = Math.max(0L, imageBytes - scoreBytes);
      System.out.println("Size Split: driver~" + driverBytes + " bytes, score~" + scoreBytes + " bytes");
      System.out.println("Score Data: voice-events=" + stats.voiceEventBytes() + " bytes, tables="
          + stats.tableBytes() + " bytes");
      System.out.println("Driver Data: note-freq-table=" + stats.noteFreqTableBytes() + " bytes");
    } else {
      System.out.println("Size Split: unavailable for backend '" + driver.id() + "'");
    }

    if (sidPath != null && Files.exists(sidPath)) {
      System.out.println("SID Size: " + Files.size(sidPath) + " bytes");
    }
  }

  private static long prgImageBytes(Path prgPath) throws Exception {
    byte[] bytes = Files.readAllBytes(prgPath);
    if (bytes.length < 2) {
      throw new IllegalStateException("PRG too small: " + prgPath);
    }
    return bytes.length - 2L;
  }

  private static int read16le(Path path) throws Exception {
    byte[] bytes = Files.readAllBytes(path);
    if (bytes.length < 2) {
      throw new IllegalStateException("File too small: " + path);
    }
    return (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8);
  }

  private static String hex4(int v) {
    return String.format("%04x", v & 0xFFFF);
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
