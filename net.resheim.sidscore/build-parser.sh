#!/bin/bash
antlr4 -o src/main/java/net/resheim/sidscore/parser \
    -package net.resheim.sidscore.parser SIDScoreLexer.g4
antlr4 -o src/main/java/net/resheim/sidscore/parser \
    -package net.resheim.sidscore.parser SIDScoreParser.g4

# Keep generated Java formatting consistent with the checked-in sources.
python3 - <<'PY'
from pathlib import Path
for path in Path("src/main/java/net/resheim/sidscore/parser").glob("*.java"):
    path.write_text("\n".join(line.rstrip() for line in path.read_text().split("\n")))
PY
