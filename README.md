![SIDScore](docs/banner.png)

SIDScore is a Java/ANTLR-based DSL and toolchain for producing music and sound effects for the Commodore 64 SID (MOS6581). It lets you write scores, define instruments, audition them in a realtime player, and export to C64-ready formats.

### What it includes

- A SID-aware DSL with instruments, tables/sequences, and reusable imports for instrument definitions.
- A basic MOS6581 synthesizer/emulator implemented in Java.
- A realtime player UI with editor, auto-reload, oscilloscope per voice, and example browser.
- Exporters that produce MOS6502 assembly + player (driver) code, plus `*.prg`, `*.sid`, and `.wav` output (via KickAssembler for PRG/SID).
- A growing collection of examples (SFX and melodies), including pieces derived from MIDI and sheet music.

### Quick start

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

### Documentation

- `SIDScore_Language_Specification.md` describes the language and instrument system in detail.

### Realtime player UI

The GUI player lets you edit and audition scores interactively:

- Editor with auto-reload for fast iteration.
- Play/Stop controls plus a file loader.
- Examples navigator that loads and plays on activation.
- Three voice oscilloscope views for quick feedback.


## Resources

- [The Sound Interface Device on C64 Wiki](https://www.c64-wiki.com/wiki/SID)
- [MUTOPIA Project – Free Sheet Music for Everyone](https://www.mutopiaproject.org/index.html)
- [How To Read Sheet Music: A Step-by-Step Guide](https://www.musicnotes.com/blog/how-to-read-sheet-music/)
