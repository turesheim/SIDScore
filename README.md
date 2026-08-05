![SIDScore](docs/banner.png)

SIDScore is a Java/ANTLR-based DSL and toolchain for producing music and sound effects for the Commodore 64 SID. It lets you parse and validate scores, define instruments, audition or serve them through SRAP, translate from other sources, read live MIDI input, and export to C64-ready formats including assembly files that can be used in your own projects.

In SIDScore, a **renderer** is used for auditioning during development, while a **driver** is the generated 6502 playback program that runs your exported score in `ASM/PRG/SID` output. In short: renderer = monitoring path, driver = runtime path for PSID/C64 targets.

**What this project includes:**

- A SID-aware [Domain Specific Language (DSL)](SIDScore_Language_Specification.md) with instruments, tables/sequences, and reusable imports.
- A score renderer with 6581 model support for auditioning while composing masterpieces, the _SIDScore Realtime Audio Player_ (SRAP).
- A local SRAP player server for protocol clients that need playback, telemetry, MIDI routing, and score-map data.
- Live MIDI input routing for CLI playback and SRAP protocol serving.
- Export pipeline for `ASM`, `PRG`, and `SID` (KickAssembler used for PRG/SID assembly).
- Source translation tooling and example libraries, including MIDI/sheet-derived pieces and SID conversion examples.

## Quick start

Build the module:

```sh
mvn -f net.resheim.sidscore/pom.xml -q package
```

Run the CLI from repo root (example file):

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI examples/test.sidscore
```

Use `--no-play` with `--wav`, `--asm`, `--prg`, or `--sid` to export without realtime audio.

Bundle multiple tunes/SFX into one multi-tune SID (subtunes):

```sh
java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar \
  net.resheim.sidscore.SIDScoreCLI examples/test.sidscore \
  --stitch examples/sfx/alert.sidscore \
  --stitch examples/J.S.Bach/bwv794.sidscore \
  --sid out-bundle.sid --no-play
```

`--stitch` currently targets SID export only (use with `--no-play`) and packs each input as one PSID subtune.

You can also define subtunes directly in the language:

```sidscore
IMPORT "../sfx/alert.sidscore" AS 2
IMPORT "../sfx/click.sidscore" AS 3
```

When `IMPORT ... AS ...` is present, SID export automatically builds a multi-tune SID.

You can also define multiple tunes inline in one file:

```sidscore
TUNE 2 {
  TEMPO 150
  VOICE 1 lead: C4 E4 G4
}
```

Inline `TUNE` blocks share top-level instruments/tables and become PSID subtunes.
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

`--midi-map` uses `voice:channel` pairs. The example above maps SID voices 1, 2, and 3 to MIDI channel 1 for three-voice polyphony from a single keyboard channel. Use `1:1,2:2,3:3` to control each SID voice from a separate MIDI channel. Live MIDI uses the mapped voice's instrument definition from the loaded score. If omitted, the default map is `1:1`; MIDI output is not supported.

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

Regenerate the generated `.sidscore` files when conversion logic, playback semantics, or SIDScore language behavior changes in a way that can affect output:

```sh
python3 tools/sid2sidscore.py examples/games/Great_Giana_Sisters.sid \
  -o examples/games/Great_Giana_Sisters.sidscore
```

By default the converter keeps simple melodic voices as notation, but emits voices with frame-level SID register automation as `EFFECT` timelines. This preserves driver instruments such as pulse-width/gate hi-hats that cannot be represented by one static `INSTR`. Use `--compact-notation` only when a smaller, less register-accurate transcription is preferred.

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

- `sidscore` is the reference export backend. It prioritizes SRAP parity and PSID/C64 compatibility over emulating legacy tracker driver quirks. It does a pretty good job on emulating plain ADSR and waveforms.
- Shared timing model: score events are compiled to frame events once and reused by both SRAP and the assembler exporter, so note/gate timing stays aligned.
- The generated driver keeps per-voice state and updates SID registers for note frequency, gate/wave control, PWM, pitch, and filter sequences on each play tick.
- Output behavior (`ASM/PRG`): standalone builds include IRQ installation for native C64 execution.
- Output behavior (`SID`): builds skip IRQ installation and rely on player callbacks (`init`/`play`) for PSID compatibility.
- Compatibility target: PSID metadata is emitted for common players (for example VSID/VICE).
- Compatibility target: SID model metadata (6581/8580) is carried through export settings.
- Introspection: CLI export reports compiled backend id and size stats (program size, estimated driver/score split, and SID size) to make regressions easier to spot.


## Resources

- [The Sound Interface Device on C64 Wiki](https://www.c64-wiki.com/wiki/SID)
- [MUTOPIA Project – Free Sheet Music for Everyone](https://www.mutopiaproject.org/index.html)
- [How To Read Sheet Music: A Step-by-Step Guide](https://www.musicnotes.com/blog/how-to-read-sheet-music/)
