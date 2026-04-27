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

- Java requirement:
  - JDK 21 (module is compiled with `--release 21`).
  - If multiple JDKs are installed, set `JAVA_HOME=$(/usr/libexec/java_home -v 21)` before Maven commands.
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
- For every `.sid` file added under `examples/games/`, generate or update the adjacent `.sidscore` file with the same basename.
- When changes affect SID conversion, playback, or export semantics, regenerate all generated `examples/games/*.sidscore` files from their source `.sid` files and revalidate them.

## Tests

- No automated tests are present. Validate by building and running the CLI on an example file.

## App Summary PDFs

- When asked to create a one-page app-summary PDF, use a shared visual template instead of inventing a new style per project.
- The canonical style reference is `readly-remarkable/output/pdf/readly-remarkable-app-summary.pdf`.
- Default output characteristics:
  - A4, exactly one page.
  - Sans-serif typography.
  - Light blue/gray header area with a stronger blue accent.
  - Uppercase blue section headings.
  - Content arranged as compact cards/panels with subtle borders/background fills.
  - Short, scannable bullets; avoid dense paragraphs.
- Default content structure:
  - Project name + one-line subtitle at top.
  - `WHAT IT IS`
  - `WHAT IT DOES`
  - `WHO IT'S FOR`
  - `HOW IT WORKS`
  - `HOW TO RUN`
  - Footer with evidence scope / repo sources used.
- Content rules:
  - Base the summary only on repo evidence.
  - If key information is missing, write `Not found in repo`.
  - Keep architecture descriptions concrete: components, files, data flow, and runtime/export path.
  - Keep getting-started steps minimal and directly runnable.
- Process rules:
  - Write intermediate sources under `tmp/pdfs/`.
  - Write final artifacts under `output/pdf/`.
  - Keep filenames stable and descriptive, normally `<repo>-app-summary.pdf`.
  - Render the generated PDF to PNG and visually inspect it before delivery.
  - If a prior app-summary PDF is supplied as a reference, match that layout/style unless the user explicitly asks for a different design.
