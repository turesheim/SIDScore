#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTDIR="$ROOT/listening-tests"
MODULE="$ROOT/net.resheim.sidscore"
WAVEFORMS="$ROOT/waveforms"
CLASSPATH="$MODULE/bin/classes:$MODULE/lib/antlr-runtime-4.13.1.jar"
CLI="net.resheim.sidscore.SIDScoreCLI"

mkdir -p "$OUTDIR"

# Build classes (required for CLI and ASM/SID export).
mvn -f "$MODULE/pom.xml" -q package

run_pair() {
  local src="$1"
  local base="$2"
  local model="$3"

  # Hardware-like render (MAME waveform tables).
  (cd "$MODULE" && \
    java -cp "$CLASSPATH" "$CLI" "$src" \
      --sid "$OUTDIR/${base}-${model}.sid" \
      --wav "$OUTDIR/${base}-${model}.wav" \
      --sid-model "$model" \
      --sid-waveforms "$WAVEFORMS" \
      --no-play)

  # Clean RTAP render (internal waveform generator).
  (cd "$MODULE" && \
    java -cp "$CLASSPATH" "$CLI" "$src" \
      --wav "$OUTDIR/${base}-rtap-${model}.wav" \
      --sid-model "$model" \
      --no-play)
}

run_pair "$ROOT/examples/W.A.Mozart/menuet_k2-simple.sidscore" "menuet_k2-simple" "6581"
run_pair "$ROOT/examples/W.A.Mozart/menuet_k2-simple.sidscore" "menuet_k2-simple" "8580"
run_pair "$ROOT/examples/W.A.Mozart/menuet_k2.sidscore" "menuet_k2" "6581"
run_pair "$ROOT/examples/W.A.Mozart/menuet_k2.sidscore" "menuet_k2" "8580"

echo "Done. Files written to $OUTDIR"
