#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTDIR="$ROOT/listening-tests"
MODULE="$ROOT/net.resheim.sidscore"
WAVEFORMS="$ROOT/waveforms"
CLASSPATH="$MODULE/bin/classes:$MODULE/lib/antlr-runtime-4.13.1.jar"
CLI="net.resheim.sidscore.SIDScoreCLI"

# Force Java 21 so Maven compile target matches runtime.
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  JAVA21_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
  if [[ -z "${JAVA21_HOME}" ]]; then
    echo "Error: Java 21 not found. Install JDK 21 and retry." >&2
    exit 1
  fi
  export JAVA_HOME="${JAVA21_HOME}"
  export PATH="${JAVA_HOME}/bin:${PATH}"
fi

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

run_pair_if_exists() {
  local src="$1"
  local base="$2"
  local model="$3"
  if [[ -f "$src" ]]; then
    run_pair "$src" "$base" "$model"
  else
    echo "Skipping missing source: $src"
  fi
}

run_pair_if_exists "$ROOT/examples/W.A.Mozart/menuet_k2-simple.sidscore" "menuet_k2-simple" "6581"
run_pair_if_exists "$ROOT/examples/W.A.Mozart/menuet_k2-simple.sidscore" "menuet_k2-simple" "8580"
run_pair_if_exists "$ROOT/examples/W.A.Mozart/menuet_k2.sidscore" "menuet_k2" "6581"
run_pair_if_exists "$ROOT/examples/W.A.Mozart/menuet_k2.sidscore" "menuet_k2" "8580"

echo "Done. Files written to $OUTDIR"
