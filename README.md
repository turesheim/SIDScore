![SIDScore](docs/banner.png)

SIDScore is a Java/ANTLR-based DSL and toolchain for producing music and sound effects for the Commodore 64 SID. It lets you parse and validate scores, define instruments, audition or serve them through SRAP, translate from other sources, read live MIDI input, and export to C64-ready formats including assembly files that can be used in your own projects.

In SIDScore, a **renderer** is used for auditioning during development, while a **driver** is the generated 6502 playback program that runs your exported score in `ASM/PRG/SID` output. SRAP also renders WAV files; the export driver runs on the C64 or in a SID player.

**What this project includes:**

- A SID-aware [Domain Specific Language (DSL)](SIDScore_Language_Specification.md) with instruments, tables/sequences, and reusable imports.
- A score renderer with approximate 6581/8580 model support for auditioning, the _SIDScore Realtime Audio Player_ (SRAP).
- A local SRAP player server for protocol clients that need playback, telemetry, MIDI routing, and score-map data.
- Live MIDI input routing for CLI playback and SRAP protocol serving.
- Export pipeline for `ASM`, `PRG`, and `SID` (KickAssembler used for PRG/SID assembly).
- Source translation tooling and example libraries, including MIDI/sheet-derived pieces and SID conversion examples.

## Quick start

Use JDK 21 and Maven, and run the commands below from the repository root. On macOS with multiple JDKs installed, select JDK 21 before building and running:

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

Build the module (including JUnit tests; the module runtime tests also require `python3`):

```sh
mvn -f net.resheim.sidscore/pom.xml -q package
```

Run the CLI from repo root (example file):

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI examples/test.sidscore
```

Use `--no-play` with `--wav`, `--asm`, `--prg`, or `--sid` to export without realtime audio.

Bundle multiple musical scores into one multi-tune SID (subtunes):

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI examples/test.sidscore \
  --stitch examples/W.A.Mozart/menuet_k2.sidscore \
  --stitch examples/J.S.Bach/bwv794_demo.sidscore \
  --sid out-bundle.sid --no-play
```

`--stitch` currently targets SID export only (use with `--no-play`) and packs each input as one PSID subtune.

You can also define subtunes directly in the language. For a score saved under `examples/`, add imports such as these; paths are relative to the importing file:

```sidscore
IMPORT "W.A.Mozart/menuet_k2.sidscore" AS 2
IMPORT "J.S.Bach/bwv794_demo.sidscore" AS 3
```

When `IMPORT ... AS ...` is present, SID export automatically builds a multi-tune SID.

You can also define multiple tunes inline in one file:

```sidscore
TEMPO 120
SYSTEM PAL

INSTR lead WAVE=TRI ADSR=0,4,8,2
VOICE 1 lead: C4 E4 G4

TUNE 2 {
  TEMPO 150
  VOICE 1 lead: G4 E4 C4
}
```

Inline `TUNE` blocks share top-level instruments/tables. `IMPORT ... AS ...` and inline `TUNE` declarations also produce a selectable multi-tune player for CLI ASM/PRG export; SID export produces PSID subtunes.
Subtune numbers (from `TUNE`, `IMPORT`, and optional `--stitch`) must be contiguous starting at `1`.

Number notation in `.sidscore`:

- Decimal values are unprefixed (for example `600`).
- Hex values must be `$`-prefixed (for example `$0800`).

List available driver backends:

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI --list-drivers
```

Select a backend explicitly (defaults to `sidscore`):

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI examples/test.sidscore --driver sidscore --sid out.sid --no-play
```

## Playback and export support

SRAP playback and WAV rendering support musical `VOICE` bodies and `EFFECT` timelines. The current CLI `--asm`, `--prg` and `--sid` export path compiles musical voices and their instrument sequences, but omits `EFFECT` timelines. An effect-only score therefore exports a silent player through that path; a mixed score exports its musical voices without its effects.

Use the embedded module API below when exporting callable effects for a host C64 program. Converted game scores can contain substantial `EFFECT` timelines, so hearing them in SRAP does not establish that an ordinary CLI export will reproduce them.

Use `--tune N` to audition or render a particular tune. With multi-tune SID export, it selects the PSID's default subtune while retaining all tunes. ASM/PRG export of a score with declared subtunes includes a tune selector rather than exporting only the requested tune.

## Embed music and sound effects in a C64 program

Use a relocatable module to play SIDScore music and trigger sound effects from a program that already manages its own frame IRQ. An effect can temporarily take over a SID voice; when it finishes, the music continues at its current position. Effects from every subtune share the same voice allocation and priority rules.

`SIDScoreModuleExporter.generate(sourcePath, namespace)` returns KickAssembler source. The host chooses the load address, calls `namespace.init` once and `namespace.play` once per video frame. A selects a one-based tune number on init; zero or an invalid number selects tune 1. Initialization starts effects declared in that tune.

After building with JDK 21, generate and assemble the included embedding example:

```sh
mkdir -p /tmp/sidscore-module-demo
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  examples/kickassembler/ExportModule.java examples/sfx/effects.sidscore \
  /tmp/sidscore-module-demo/score.asm
cp examples/kickassembler/module-host.asm /tmp/sidscore-module-demo/host.asm
java -jar net.resheim.sidscore/lib/KickAss.jar /tmp/sidscore-module-demo/host.asm \
  -o /tmp/sidscore-module-demo/host.prg
```

[ExportModule.java](examples/kickassembler/ExportModule.java) demonstrates the Java API. [module-host.asm](examples/kickassembler/module-host.asm) places the module after the host routines at `$3000`, initializes tune 1 and exposes a frame callback. It is an embedding scaffold: your program calls `start` once, calls `on_frame` from its frame IRQ, and calls `trigger_noise` when needed.

Trigger an effect with `jsr Score.tuneN.effect_Name`, even when that tune is not selected. Effects from tune 1 also have `Score.effect_Name` aliases. `RESTART` restarts an active effect, `IGNORE` leaves it running, and `STEAL` can replace a lower-priority effect on a busy voice. A trigger requiring an occupied voice can be rejected. Global-only filter/volume effects do not reserve a voice.

Reserve zero-page `$fb-$fc` while module routines execute; the frame callback restores their previous values. The module uses SID registers `$d400-$d418` and leaves IRQ installation to the host. Calls can change A/X/Y and arithmetic flags, so preserve the host's registers around an IRQ callback and serialize triggers with playback. Call `play` at the source's PAL or NTSC frame rate.

A module supports up to 254 tunes and 255 effects in total. Each effect can contain 1–4096 frames; long register-trace effects from converted game music may exceed this limit. Shorten or split those effects before module export.

## Live MIDI input

SRAP can use a USB MIDI keyboard or controller, such as an Arturia MicroLab, as a live input source. MIDI is a realtime audition and protocol-serving feature and does not change `ASM/PRG/SID` export.

List available MIDI input devices:

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI --list-midi-devices
```

Probe incoming MIDI events without starting score playback:

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI --midi-probe --midi-device MicroLab --seconds 20
```

If a controller exposes multiple input endpoints, probe all of them and press a
few keys:

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI --midi-probe --all --seconds 20
```

`RX NOTE_ON ch N` confirms Java is receiving the keyboard and tells you which
channel to use in `--midi-map`. If no `RX` lines appear for any endpoint, the
host Java Sound layer is not delivering MIDI callbacks for that device.

Play a score with live MIDI control:

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI examples/test.sidscore \
  --midi --midi-device MicroLab --midi-map 1:1,2:1,3:1
```

`--midi-map` uses `voice:channel` pairs. The example above maps SID voices 1, 2, and 3 to MIDI channel 1 for three-voice polyphony from a single keyboard channel. Use `1:1,2:2,3:3` to control each SID voice from a separate MIDI channel. Live MIDI uses the mapped voice's instrument definition from the loaded score. If omitted, the default map is `1:1`; MIDI output is not supported. Live MIDI playback cannot be combined with `--no-play` or WAV recording.

## SID model

The CLI defaults to the 6581 model. Use `--sid-model 8580` for the 8580 renderer and PSID model metadata:

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI examples/test.sidscore \
  --sid-model 8580 --wav /tmp/sidscore-8580.wav --no-play
```

SRAP uses an approximate SID synthesizer, so its sound is an audition of the score rather than a guarantee of identical output on physical hardware. `--sid-model` also sets the model metadata for SID files; ASM/PRG output runs on the machine's installed SID chip.

## SRAP Player Server

SIDScore can run as a local SRAP server for external tools. The server accepts score playback commands, source playback commands, instrument overrides, MIDI settings, device scans, and telemetry subscriptions. See [SIDScore_Server_Specification.md](SIDScore_Server_Specification.md) for frame formats and protocol details.

The graphical SIDScore playback, MIDI routing, and live instrument editor UI now live in [Commodore Commander](https://github.com/turesheim/commodore-commander). SIDScore remains the parser, renderer, SRAP server, and export toolchain; use Commodore Commander when you want the integrated IDE controls for auditioning and editing live instrument overrides.

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI --player-server --port 0
```

Use `--midi`, `--midi-device`, and `--midi-map` with `--player-server` to start with live MIDI input enabled.

## SID Conversion Examples

Game SID examples under `examples/games/` are generated with `tools/sid2sidscore.py` from the matching `.sid` files. Each `.sid` added there must have a generated `.sidscore` next to it.

Regenerate all generated game `.sidscore` files when conversion logic, playback semantics, or SIDScore language behavior changes in a way that can affect output. With a matching local HVSC collection, convert a file using:

```sh
python3 tools/sid2sidscore.py examples/games/Great_Giana_Sisters.sid \
  --hvsc-root ~/Music/C64music -o /tmp/Great_Giana_Sisters.sidscore
```

The converter looks for a matching SID in `~/Music/C64music` by default and reads lengths/comments from its HVSC metadata. Without matching metadata or explicit `--lengths`, it falls back to 60 seconds per subtune (`--seconds` changes the fallback). That can change tune lengths and whether short subtunes become effects. If HVSC is unavailable, supply the original per-tune lengths with `--lengths`; it does not automatically reuse lengths or comments from an existing `.sidscore` file. Inspect the conversion header and validate the output before replacing the checked-in example.

By default the converter keeps simple melodic voices as notation, but emits voices with frame-level SID register automation as `EFFECT` timelines. This preserves automation such as pulse-width/gate hi-hats for SRAP/WAV playback and, within its limits, embedded module export. Use `--compact-notation` only when a smaller, less register-accurate transcription is preferred.

When a melodic trace contains stable fine pitch oscillation around a note, the converter may emit
`VIBRATO=delay,rate,amp,inc` on the generated `INSTR`. Less regular pitch movement stays in notation or
`EFFECT` form to avoid misclassifying slides and arpeggios as vibrato.

## Eclipse Generic Editor

SIDScore includes a TextMate grammar at `syntaxes/sidscore.tmLanguage.json` for Eclipse TM4E / Generic Editor use.
The grammar file by itself is not enough: Eclipse must also be told to open `.sidscore` files with the Generic Editor and to use that grammar for the file association or content type your installation uses.

Typical setup procedure:

1. Install TM4E support in Eclipse if it is not already present.
2. In Eclipse, associate `*.sidscore` with the `Generic Editor` via `Open With` or the file-association preferences.
3. In the TM4E/TextMate preferences, register `syntaxes/sidscore.tmLanguage.json` as a grammar with scope name `source.sidscore`.
4. Bind that grammar to the file association or content type used for `.sidscore` files in your Eclipse/TM4E installation.
5. Reopen the file in the `Generic Editor`.

The exact preference page names vary between Eclipse/TM4E versions, but if highlighting does not appear, the problem is usually one of these:

- `.sidscore` is opening in a different editor.
- The grammar was added, but not bound to the file association or content type Eclipse is using.
- The file needs to be reopened after the grammar registration changes.

## Technical Details

### `sidscore` Driver

- `sidscore` is the built-in export backend. Additional backends can be provided through Java's `ServiceLoader`.
- Shared timing model: SRAP and the assembler exporter use `FrameEventCompiler` to compile musical voice events, keeping their base note/gate timing aligned. Effect playback uses a separate runtime and is subject to the support limits above.
- The generated driver keeps per-voice state and updates SID registers for note frequency, gate/wave control, PWM, pitch, and filter sequences on each play tick.
- Output behavior (`ASM/PRG`): standalone builds include IRQ installation for native C64 execution.
- Output behavior (`SID`): builds skip IRQ installation and rely on player callbacks (`init`/`play`) for PSID compatibility.
- Compatibility target: PSID metadata is emitted for common players (for example VSID/VICE).
- Compatibility target: SID model metadata (6581/8580) is carried through export settings.
- Introspection: CLI export reports compiled backend id and size stats (program size, estimated driver/score split, and SID size) to make regressions easier to spot.


## Validation and parser generation

Run the Java tests with the Maven build above and the converter tests with:

```sh
python3 -m unittest discover -s tools -p 'test_*.py'
```

SRAP tests bind local loopback ports. Export tests use the bundled KickAssembler jar, and module runtime tests run the repository's 6502 emulator through `python3`.

Generated ANTLR parser sources are checked in. If you change the `.g4` grammars, regenerate them with ANTLR 4.13.1 available as `antlr4` on PATH:

```sh
cd net.resheim.sidscore
./build-parser.sh
```

## Resources

- [The Sound Interface Device on C64 Wiki](https://www.c64-wiki.com/wiki/SID)
- [MUTOPIA Project – Free Sheet Music for Everyone](https://www.mutopiaproject.org/index.html)
- [How To Read Sheet Music: A Step-by-Step Guide](https://www.musicnotes.com/blog/how-to-read-sheet-music/)
