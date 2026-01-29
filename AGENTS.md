# AGENTS

## Project overview

- SIDScore is a Java/ANTLR-based DSL and toolchain for composing Commodore 64 music and sound effects.
- The main CLI entry point is `net.resheim.sidscore.SIDScoreCLI`.

## Repository layout

- `net.resheim.sidscore/` Maven module with sources, grammar, and build scripts.
- `net.resheim.sidscore/src/main/java/net/resheim/sidscore/` core Java code.
- `net.resheim.sidscore/SIDScoreLexer.g4` and `net.resheim.sidscore/SIDScoreParser.g4` ANTLR grammars.
- `net.resheim.sidscore/src/main/java/net/resheim/sidscore/parser/` generated parser code.
- `net.resheim.sidscore/lib/` local runtime jars (ANTLR runtime, KickAss assembler).
- `examples/` sample `.sidscore` files.
- `SIDScore_Language_Specification.md` language reference; this is the foundation for the parser and player behavior.

## Build and run

- Build module:
  - `mvn -f net.resheim.sidscore/pom.xml -q package`
- Regenerate parser (only if the `.g4` grammars change; requires `antlr4` on PATH):
  - `cd net.resheim.sidscore && ./build-parser.sh`
- Run CLI from repo root after build:
  - `java -cp net.resheim.sidscore/bin/classes:net.resheim.sidscore/lib/antlr-runtime-4.13.1.jar net.resheim.sidscore.SIDScoreCLI examples/test.sidscore`
  - Use `--no-play` with `--wav`, `--asm`, `--prg`, or `--sid` to avoid realtime audio.

## Notes and conventions

- Do not edit generated parser files directly; update `.g4` sources and rerun `build-parser.sh`.
- `net.resheim.sidscore/bin` is the build output; avoid manual edits.
- Match existing Java formatting and keep changes minimal and localized.

## Tests

- No automated tests are present. Validate by building and running the CLI on an example file.
