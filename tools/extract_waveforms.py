#!/usr/bin/env python3
"""
Extract SID combined waveform tables from MAME headers into waveforms/*.bin.

Usage:
  python3 tools/extract_waveforms.py
"""
from __future__ import annotations

import re
from pathlib import Path


def eval_size(expr: str) -> int:
    parts = [p.strip() for p in expr.split("+") if p.strip()]
    total = 0
    for p in parts:
        if p.startswith(("0x", "0X")):
            total += int(p[2:], 16)
        else:
            total += int(p)
    return total


def parse_arrays(text: str) -> dict[str, tuple[int, list[int]]]:
    array_re = re.compile(
        r"static\s+const\s+uint8_t\s+(waveform\d+_\d+)\s*\[\s*([^\]]+)\s*\]\s*=\s*\{(.*?)\};",
        re.S,
    )
    num_re = re.compile(r"0x[0-9a-fA-F]+|\d+")
    out: dict[str, tuple[int, list[int]]] = {}
    for m in array_re.finditer(text):
        name = m.group(1)
        expected = eval_size(m.group(2))
        body = m.group(3)
        nums = num_re.findall(body)
        vals: list[int] = []
        for tok in nums:
            if tok.startswith(("0x", "0X")):
                vals.append(int(tok[2:], 16) & 0xFF)
            else:
                vals.append(int(tok) & 0xFF)
        if len(vals) > expected:
            raise SystemExit(f"{name}: expected {expected} values, got {len(vals)}")
        if len(vals) < expected:
            vals.extend([0] * (expected - len(vals)))
        out[name] = (expected, vals)
    return out


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    wave_dir = root / "waveforms"
    wave_dir.mkdir(exist_ok=True)

    headers = [
        root / "externals/mame/src/devices/sound/sidw6581.h",
        root / "externals/mame/src/devices/sound/sidw8580.h",
    ]

    extracted: dict[str, Path] = {}
    for header in headers:
        text = header.read_text()
        arrays = parse_arrays(text)
        if not arrays:
            raise SystemExit(f"No waveform arrays found in {header}")
        for name, (_, vals) in arrays.items():
            out = wave_dir / f"{name}.bin"
            out.write_bytes(bytes(vals))
            extracted[name] = out

    license_text = """Waveform binaries license and provenance

Source headers:
- externals/mame/src/devices/sound/sidw6581.h
- externals/mame/src/devices/sound/sidw8580.h

Both headers declare:
- license: GPL-2.0+
- copyright-holders: Dag Lem

The 6581 header also states:
\"Read-out combined waveforms taken from reSID 0.5.\"

The 8580 header also states:
\"MOS-8580 R5 waveforms $30,$50,$60,$70\" and
\"Created with Deadman's Raw Data to C Header converter\"

Binary files in this directory (all GPL-2.0+):
- waveform30_6581.bin (4096 bytes)
  Source: waveform30_6581[] in sidw6581.h (GPL-2.0+).
- waveform50_6581.bin (8192 bytes)
  Source: waveform50_6581[] in sidw6581.h (GPL-2.0+).
  Note: The initializer in the header provides 4096 values; the remaining
  4096 bytes are zero-filled to match C initializer semantics for an array
  of size 4096+4096.
- waveform60_6581.bin (8192 bytes)
  Source: waveform60_6581[] in sidw6581.h (GPL-2.0+).
  Note: The initializer in the header provides 4096 values; the remaining
  4096 bytes are zero-filled to match C initializer semantics for an array
  of size 4096+4096.
- waveform70_6581.bin (8192 bytes)
  Source: waveform70_6581[] in sidw6581.h (GPL-2.0+).
  Note: The initializer in the header provides 4096 values; the remaining
  4096 bytes are zero-filled to match C initializer semantics for an array
  of size 4096+4096.
- waveform30_8580.bin (4096 bytes)
  Source: waveform30_8580[] in sidw8580.h (GPL-2.0+).
- waveform50_8580.bin (8192 bytes)
  Source: waveform50_8580[] in sidw8580.h (GPL-2.0+).
  Note: The initializer in the header provides 4096 values; the remaining
  4096 bytes are zero-filled to match C initializer semantics for an array
  of size 4096+4096.
- waveform60_8580.bin (8192 bytes)
  Source: waveform60_8580[] in sidw8580.h (GPL-2.0+).
  Note: The initializer in the header provides 4096 values; the remaining
  4096 bytes are zero-filled to match C initializer semantics for an array
  of size 4096+4096.
- waveform70_8580.bin (8192 bytes)
  Source: waveform70_8580[] in sidw8580.h (GPL-2.0+).
  Note: The initializer in the header provides 4096 values; the remaining
  4096 bytes are zero-filled to match C initializer semantics for an array
  of size 4096+4096.
"""

    (wave_dir / "LICENSE.txt").write_text(license_text)
    print(f"Wrote {len(extracted)} waveform binaries to {wave_dir}")


if __name__ == "__main__":
    main()
