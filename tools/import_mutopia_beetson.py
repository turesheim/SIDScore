#!/usr/bin/env python3
"""
Download Mutopia Beethoven sonata MIDI files and convert them to SIDScore.

Default source:
  https://www.mutopiaproject.org/cgibin/make-table.cgi?collection=beetson&preview=1
"""

from __future__ import annotations

import argparse
import html
import re
import subprocess
import sys
import unicodedata
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable
from urllib.parse import urlparse
from urllib.request import Request, urlopen


DEFAULT_PAGE_URL = "https://www.mutopiaproject.org/cgibin/make-table.cgi?collection=beetson&preview=1"
USER_AGENT = "sidscore-mutopia-import/1.0"
INSTRUMENT_REL_PATH = "../instruments/piano_family.sidscore"

PIANO_FAMILY_SIDSCORE = """; Shared piano-like instruments for Beethoven sonata imports.
; Tuning basis from classic C64 references:
; - C64 User's Guide "simulate instruments": piano = pulse, AD=9, SR=0,
;   pulse Hi=0, Lo=255.
; - C64 Programmer's Reference Guide "piano type sound":
;   pulse with A=0 D=9 S=0 R=0, PW around $0800.
; To reduce hard clicks in dense passages, we keep release at 1 on upper/mid
; and 2 on bass while preserving the short-decay piano contour.

TABLE wave PianoHiWave {
  WAVE=SAW+PULSE @1
  WAVE=PULSE HOLD
}

TABLE wave PianoMidWave {
  WAVE=PULSE HOLD
}

TABLE wave PianoLowWave {
  WAVE=TRI+PULSE @1
  WAVE=PULSE HOLD
}

TABLE pw PianoHiPW {
  $0780 @2
  $0800 @2
  $0840 @2
  $0800 @2
  LOOP
}

TABLE pw PianoMidPW {
  $00FF @3
  $0120 @3
  $00FF @3
  LOOP
}

TABLE pw PianoLowPW {
  $0200 @4
  $0240 @4
  $0200 @4
  LOOP
}

INSTR piano_hi
  WAVE=PULSE ADSR=0,9,0,1 PW=$0800
  WAVESEQ=PianoHiWave PWSEQ=PianoHiPW

INSTR piano_mid
  WAVE=PULSE ADSR=0,9,0,1 PW=$00FF
  WAVESEQ=PianoMidWave PWSEQ=PianoMidPW

INSTR piano_low
  WAVE=PULSE ADSR=0,8,1,2 PW=$0200
  WAVESEQ=PianoLowWave PWSEQ=PianoLowPW
"""


@dataclass
class PieceLink:
    title: str
    mid_url: str
    stem: str


@dataclass
class NoteEvent:
    start_tick: int
    end_tick: int
    pitch: int


@dataclass
class MidiSong:
    ppq: int
    tempo_bpm: int
    time_num: int
    time_den: int
    notes: list[NoteEvent]


def fetch_text(url: str) -> str:
    if url.startswith("http://") or url.startswith("https://"):
        cp = subprocess.run(
            ["curl", "-fsSL", "-A", USER_AGENT, url],
            check=True,
            capture_output=True,
        )
        return cp.stdout.decode("utf-8", errors="replace")
    req = Request(url, headers={"User-Agent": USER_AGENT})
    with urlopen(req) as r:
        data = r.read()
    return data.decode("utf-8", errors="replace")


def fetch_bytes(url: str) -> bytes:
    if url.startswith("http://") or url.startswith("https://"):
        cp = subprocess.run(
            ["curl", "-fsSL", "-A", USER_AGENT, url],
            check=True,
            capture_output=True,
        )
        return cp.stdout
    req = Request(url, headers={"User-Agent": USER_AGENT})
    with urlopen(req) as r:
        return r.read()


def normalize_spaces(s: str) -> str:
    return re.sub(r"\s+", " ", s).strip()


def ascii_safe(s: str) -> str:
    # Keep files ASCII-friendly for SIDScore sources and docs.
    replacements = {
        "\u2013": "-",
        "\u2014": "-",
        "\u2018": "'",
        "\u2019": "'",
        "\u201c": '"',
        "\u201d": '"',
        "\u00a0": " ",
    }
    for src, dst in replacements.items():
        s = s.replace(src, dst)
    s = unicodedata.normalize("NFKD", s)
    return s.encode("ascii", "ignore").decode("ascii")


def strip_tags(raw: str) -> str:
    text = re.sub(r"<[^>]+>", "", raw)
    return normalize_spaces(ascii_safe(html.unescape(text)))


def sidscore_string(s: str) -> str:
    # SIDScore quoted strings do not support escape sequences; avoid literal quotes.
    return s.replace('"', "'")


def parse_piece_links(page_html: str) -> list[PieceLink]:
    blocks = re.findall(r'<table class="table-bordered result-table">(.*?)</table>', page_html, flags=re.S)
    out: list[PieceLink] = []
    seen: set[str] = set()
    for block in blocks:
        m_mid = re.search(r'href="(https://[^"]+\.mid)"', block)
        if not m_mid:
            continue
        mid_url = m_mid.group(1)
        if mid_url in seen:
            continue
        seen.add(mid_url)

        m_title = re.search(r"<tr><td>(.*?)</td>\s*<td>by ", block, flags=re.S)
        if m_title:
            title = strip_tags(m_title.group(1))
        else:
            title = Path(urlparse(mid_url).path).stem

        stem = Path(urlparse(mid_url).path).stem
        out.append(PieceLink(title=title, mid_url=mid_url, stem=stem))
    return out


def read_u16be(data: bytes, pos: int) -> int:
    return (data[pos] << 8) | data[pos + 1]


def read_u32be(data: bytes, pos: int) -> int:
    return (data[pos] << 24) | (data[pos + 1] << 16) | (data[pos + 2] << 8) | data[pos + 3]


def read_vlq(data: bytes, pos: int) -> tuple[int, int]:
    value = 0
    while True:
        if pos >= len(data):
            raise ValueError("Truncated VLQ")
        b = data[pos]
        pos += 1
        value = (value << 7) | (b & 0x7F)
        if (b & 0x80) == 0:
            return value, pos


def parse_midi(data: bytes) -> MidiSong:
    if len(data) < 14 or data[0:4] != b"MThd":
        raise ValueError("Invalid MIDI header")
    hdr_len = read_u32be(data, 4)
    fmt = read_u16be(data, 8)
    ntrks = read_u16be(data, 10)
    division = read_u16be(data, 12)
    if division & 0x8000:
        raise ValueError("SMPTE MIDI timing is not supported")
    ppq = division
    pos = 8 + hdr_len

    notes: list[NoteEvent] = []
    tempo_us_per_qn: int | None = None
    time_num: int | None = None
    time_den: int | None = None

    for _ in range(ntrks):
        if pos + 8 > len(data) or data[pos:pos + 4] != b"MTrk":
            raise ValueError("Invalid MIDI track chunk")
        track_len = read_u32be(data, pos + 4)
        pos += 8
        track = data[pos:pos + track_len]
        pos += track_len

        tick = 0
        tpos = 0
        running_status: int | None = None
        open_notes: dict[tuple[int, int], list[int]] = defaultdict(list)
        last_tick = 0

        while tpos < len(track):
            delta, tpos = read_vlq(track, tpos)
            tick += delta
            last_tick = tick
            if tpos >= len(track):
                break

            status = track[tpos]
            if status < 0x80:
                if running_status is None:
                    raise ValueError("Running status without prior status")
                status = running_status
            else:
                tpos += 1
                if status < 0xF0:
                    running_status = status
                else:
                    running_status = None

            if status == 0xFF:
                if tpos >= len(track):
                    break
                meta_type = track[tpos]
                tpos += 1
                mlen, tpos = read_vlq(track, tpos)
                meta = track[tpos:tpos + mlen]
                tpos += mlen
                if meta_type == 0x2F:
                    break
                if meta_type == 0x51 and mlen == 3 and tempo_us_per_qn is None:
                    tempo_us_per_qn = (meta[0] << 16) | (meta[1] << 8) | meta[2]
                if meta_type == 0x58 and mlen >= 2 and time_num is None and time_den is None:
                    time_num = meta[0]
                    time_den = 1 << meta[1]
                continue

            if status in (0xF0, 0xF7):
                syx_len, tpos = read_vlq(track, tpos)
                tpos += syx_len
                continue

            kind = status & 0xF0
            ch = status & 0x0F

            def data_byte() -> int:
                nonlocal tpos
                if tpos >= len(track):
                    raise ValueError("Unexpected end of MIDI data")
                b = track[tpos]
                tpos += 1
                return b

            if kind in (0xC0, 0xD0):
                _d1 = data_byte()
                continue

            d1 = data_byte()
            d2 = data_byte()

            if ch == 9:
                continue

            if kind == 0x90 and d2 > 0:
                open_notes[(ch, d1)].append(tick)
                continue

            if kind == 0x80 or (kind == 0x90 and d2 == 0):
                stack = open_notes.get((ch, d1))
                if stack:
                    start = stack.pop(0)
                    if tick > start:
                        notes.append(NoteEvent(start_tick=start, end_tick=tick, pitch=d1))

        # Close dangling notes at track end.
        for (ch, pitch), starts in open_notes.items():
            _ = ch
            for s in starts:
                if last_tick > s:
                    notes.append(NoteEvent(start_tick=s, end_tick=last_tick, pitch=pitch))

    if tempo_us_per_qn is None:
        tempo_bpm = 120
    else:
        tempo_bpm = int(round(60_000_000.0 / tempo_us_per_qn))
    tempo_bpm = max(30, min(tempo_bpm, 260))

    if time_num is None:
        time_num = 4
    if time_den is None:
        time_den = 4

    return MidiSong(ppq=ppq, tempo_bpm=tempo_bpm, time_num=time_num, time_den=time_den, notes=notes)


def quantize_sid_ticks(raw_tick: int, ppq: int) -> int:
    # SIDScore uses 48 ticks per quarter note (192 per whole note). Quantize to 1/64-note steps (3 ticks).
    sid_tick = raw_tick * 48.0 / ppq
    return int(round(sid_tick / 3.0)) * 3


def extract_voice_segments(song: MidiSong) -> list[list[tuple[int | None, int]]]:
    # Convert note intervals to quantized SIDScore ticks.
    qnotes: list[NoteEvent] = []
    for n in song.notes:
        s = quantize_sid_ticks(n.start_tick, song.ppq)
        e = quantize_sid_ticks(n.end_tick, song.ppq)
        if e <= s:
            e = s + 3
        qnotes.append(NoteEvent(start_tick=s, end_tick=e, pitch=n.pitch))

    if not qnotes:
        return [[(None, 192)], [(None, 192)], [(None, 192)]]

    starts: dict[int, list[int]] = defaultdict(list)
    ends: dict[int, list[int]] = defaultdict(list)
    boundaries: set[int] = {0}
    for n in qnotes:
        starts[n.start_tick].append(n.pitch)
        ends[n.end_tick].append(n.pitch)
        boundaries.add(n.start_tick)
        boundaries.add(n.end_tick)
    ticks = sorted(boundaries)

    # Merge adjacent spans into per-voice monophonic segments.
    active = Counter()
    current_pitch: list[int | None] = [None, None, None]
    current_len = [0, 0, 0]
    out: list[list[tuple[int | None, int]]] = [[], [], []]

    def choose_voices(counter: Counter, prev: list[int | None]) -> list[int | None]:
        pitches = sorted(counter.keys())
        if not pitches:
            return [None, None, None]

        # One active note: place it in the voice that best preserves continuity
        # and register intent (high/mid/low).
        if len(pitches) == 1:
            p = pitches[0]
            ideal = [84, 64, 43]
            costs: list[float] = []
            for i in range(3):
                move_cost = abs(prev[i] - p) if prev[i] is not None else 12.0
                range_cost = abs(p - ideal[i]) / 6.0
                costs.append(move_cost + range_cost)
            slot = min(range(3), key=lambda i: costs[i])
            out_single: list[int | None] = [None, None, None]
            out_single[slot] = p
            return out_single

        # Two or more: preserve outer voices for melody + bass.
        top = pitches[-1]
        low = pitches[0]
        out_multi: list[int | None] = [top, None, low]
        mids = pitches[1:-1]
        if mids:
            if prev[1] is None:
                out_multi[1] = mids[len(mids) // 2]
            else:
                out_multi[1] = min(mids, key=lambda p: abs(p - prev[1]))
        return out_multi

    for i, tick in enumerate(ticks):
        # End notes at this tick, then start notes at this tick.
        for p in ends.get(tick, []):
            if active[p] > 0:
                active[p] -= 1
                if active[p] == 0:
                    del active[p]
        for p in starts.get(tick, []):
            active[p] += 1

        if i + 1 >= len(ticks):
            break
        next_tick = ticks[i + 1]
        dur = next_tick - tick
        if dur <= 0:
            continue
        voices = choose_voices(active, current_pitch)
        for v in range(3):
            if voices[v] == current_pitch[v]:
                current_len[v] += dur
            else:
                if current_len[v] > 0:
                    out[v].append((current_pitch[v], current_len[v]))
                current_pitch[v] = voices[v]
                current_len[v] = dur

    for v in range(3):
        if current_len[v] > 0:
            out[v].append((current_pitch[v], current_len[v]))
        # Ensure non-empty voice bodies.
        if not out[v]:
            out[v].append((None, 192))
    return out


# (ticks, denominator, dotted)
DUR_PARTS: list[tuple[int, int, bool]] = [
    (288, 1, True),
    (192, 1, False),
    (144, 2, True),
    (96, 2, False),
    (72, 4, True),
    (48, 4, False),
    (36, 8, True),
    (24, 8, False),
    (18, 16, True),
    (12, 16, False),
    (9, 32, True),
    (6, 32, False),
    (3, 64, False),
]


NOTE_NAMES = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"]


def decompose_duration(ticks: int) -> list[tuple[int, int, bool]]:
    remaining = max(3, ticks)
    parts: list[tuple[int, int, bool]] = []
    while remaining > 0:
        choice = None
        for p in DUR_PARTS:
            if p[0] <= remaining:
                choice = p
                break
        if choice is None:
            choice = DUR_PARTS[-1]
        parts.append(choice)
        remaining -= choice[0]
    return parts


def pitch_to_note(pitch: int) -> tuple[int, str]:
    p = max(0, min(127, pitch))
    octave = p // 12 - 1
    name = NOTE_NAMES[p % 12]
    return octave, name


def emit_voice_tokens(segments: Iterable[tuple[int | None, int]]) -> list[str]:
    tokens: list[str] = []
    current_octave = 4
    for pitch, dur in segments:
        parts = decompose_duration(dur)
        if pitch is None:
            for _, denom, dotted in parts:
                tokens.append(f"R{denom}{'.' if dotted else ''}")
            continue

        octave, note_name = pitch_to_note(pitch)
        for i, (_, denom, dotted) in enumerate(parts):
            if octave != current_octave:
                tokens.append(f"O{octave}")
                current_octave = octave
            if i > 0:
                tokens.append("&")
            tokens.append(f"{note_name}{denom}{'.' if dotted else ''}")
    return tokens


def format_voice_tokens(tokens: list[str], indent: str = "  ", max_tokens_per_line: int = 16) -> str:
    lines: list[str] = []
    i = 0
    while i < len(tokens):
        chunk = tokens[i:i + max_tokens_per_line]
        lines.append(indent + " ".join(chunk))
        i += max_tokens_per_line
    if not lines:
        lines.append(indent + "R1")
    return "\n".join(lines)


def sidscore_text(piece: PieceLink, song: MidiSong) -> str:
    segments = extract_voice_segments(song)
    voice1 = format_voice_tokens(emit_voice_tokens(segments[0]))
    voice2 = format_voice_tokens(emit_voice_tokens(segments[1]))
    voice3 = format_voice_tokens(emit_voice_tokens(segments[2]))
    title = sidscore_string(piece.title)
    return f"""; Auto-generated from Mutopia MIDI
; Source: {piece.mid_url}
; Conversion is approximate: dense piano texture is reduced to 3 SID voices.
; Voice assignment favors top melody, bass foundation, and stable middle motion.

TITLE "{title}"
AUTHOR "L. V. Beethoven"
RELEASED "Mutopia"

TEMPO {song.tempo_bpm}
TIME {song.time_num}/{song.time_den}
SYSTEM PAL

INSTR piano_hi "{INSTRUMENT_REL_PATH}"
INSTR piano_mid "{INSTRUMENT_REL_PATH}"
INSTR piano_low "{INSTRUMENT_REL_PATH}"

VOICE 1 piano_hi:
{voice1}

VOICE 2 piano_mid:
{voice2}

VOICE 3 piano_low:
{voice3}
"""


def write_shared_instruments(out_root: Path) -> Path:
    instr_dir = out_root / "instruments"
    instr_dir.mkdir(parents=True, exist_ok=True)
    path = instr_dir / "piano_family.sidscore"
    path.write_text(PIANO_FAMILY_SIDSCORE, encoding="utf-8")
    return path


def write_readme(out_root: Path, source_page: str, pieces: list[PieceLink], instrument_file: Path) -> None:
    stamp = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    lines = [
        "# Beethoven Sonata Import (Mutopia)",
        "",
        "These files were downloaded from Mutopia and converted to SIDScore automatically.",
        "",
        f"- Source page: {source_page}",
        f"- Imported: {stamp}",
        f"- MID files: `{(out_root / 'mid').as_posix()}`",
        f"- SIDScore files: `{(out_root / 'sidscore').as_posix()}`",
        f"- Shared instrument file: `{instrument_file.as_posix()}`",
        "",
        "## Pieces",
        "",
    ]
    for p in pieces:
        lines.append(f"- `{p.stem}.mid` -> `{p.stem}.sidscore` ({p.title})")
    lines.append("")
    (out_root / "README.md").write_text("\n".join(lines), encoding="utf-8")


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--page-url", default=DEFAULT_PAGE_URL)
    ap.add_argument("--out-root", default="examples/L.v.Beethoven/beetson")
    args = ap.parse_args()

    out_root = Path(args.out_root)
    mid_dir = out_root / "mid"
    sid_dir = out_root / "sidscore"
    mid_dir.mkdir(parents=True, exist_ok=True)
    sid_dir.mkdir(parents=True, exist_ok=True)
    instrument_file = write_shared_instruments(out_root)

    page_html = fetch_text(args.page_url)
    pieces = parse_piece_links(page_html)
    if not pieces:
        print("No MIDI links found on source page.", file=sys.stderr)
        return 2

    print(f"Found {len(pieces)} MIDI links")
    for i, p in enumerate(pieces, start=1):
        mid_path = mid_dir / f"{p.stem}.mid"
        sid_path = sid_dir / f"{p.stem}.sidscore"
        print(f"[{i:02d}/{len(pieces):02d}] {p.stem}")
        if not mid_path.exists():
            mid_path.write_bytes(fetch_bytes(p.mid_url))
        song = parse_midi(mid_path.read_bytes())
        sid_path.write_text(sidscore_text(p, song), encoding="utf-8")

    write_readme(out_root, args.page_url, pieces, instrument_file)
    print(f"Wrote {len(pieces)} MID + SIDScore pairs to {out_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
