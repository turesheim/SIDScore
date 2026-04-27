#!/usr/bin/env python3
"""Deterministically convert PSID/RSID files to SIDScore.

The converter uses the local ``tools/siddump.py`` register trace as the source
of truth. Melodic SID voices are emitted as SIDScore music notation on a fixed
frame grid. NOISE-backed voices and short subtunes are emitted as EFFECT
timelines with raw SID register values.
"""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import hashlib
import math
from pathlib import Path
import re
import subprocess
import sys


PAL_FRAMES_PER_SECOND = 50
RASTER_RATE_PAL = 50.124542
RASTER_RATE_NTSC = 60.098814
SID_CLOCK_PAL = 985248.0
SID_CLOCK_NTSC = 1022727.0
DEFAULT_GRID_FRAMES = 8
DEFAULT_SECONDS = 60
DEFAULT_EFFECT_THRESHOLD_SECONDS = 4
VOICE_COUNT = 3

WAVE_BITS = (
    (0x10, "TRI"),
    (0x20, "SAW"),
    (0x40, "PULSE"),
    (0x80, "NOISE"),
)

FILTER_NAMES = {
    "Off": "OFF",
    "Low": "LP",
    "Bnd": "BP",
    "L+B": "LP+BP",
    "Hi": "HP",
    "L+H": "LP+HP",
    "B+H": "BP+HP",
    "LBH": "LP+BP+HP",
}


@dataclass(frozen=True)
class SidHeader:
    magic: str
    version: int
    data_offset: int
    load_address: int
    init_address: int
    play_address: int
    songs: int
    start_song: int
    flags: int
    title: str
    author: str
    released: str


@dataclass
class VoiceState:
    freq: int | None = None
    note: str | None = None
    wave: int | None = None
    adsr: tuple[int, int, int, int] | None = None
    pulse: int | None = None


@dataclass
class FilterState:
    cutoff: int | None = None
    res: int | None = None
    route: int | None = None
    mode: str | None = None
    volume: int | None = None


@dataclass
class DumpRow:
    frame: int
    voices: list[VoiceState]
    filter: FilterState


@dataclass(frozen=True)
class HvscMetadata:
    relative_path: str | None
    lengths: list[int] | None
    stil_lines: list[str]


def be16(data: bytes, offset: int) -> int:
    if offset + 2 > len(data):
        raise ValueError("SID header is truncated")
    return (data[offset] << 8) | data[offset + 1]


def read_sid_header(path: Path) -> SidHeader:
    data = path.read_bytes()
    if len(data) < 0x76:
        raise ValueError(f"{path} is too short to be a PSID/RSID file")
    magic = data[:4].decode("ascii", errors="replace")
    if magic not in {"PSID", "RSID"}:
        raise ValueError(f"{path} is not a PSID/RSID file")

    def sid_text(offset: int) -> str:
        raw = data[offset:offset + 32].split(b"\0", 1)[0]
        return raw.decode("latin-1", errors="replace").strip()

    load_address = be16(data, 8)
    data_offset = be16(data, 6)
    if load_address == 0 and data_offset + 2 <= len(data):
        load_address = data[data_offset] | (data[data_offset + 1] << 8)

    return SidHeader(
        magic=magic,
        version=be16(data, 4),
        data_offset=data_offset,
        load_address=load_address,
        init_address=be16(data, 10),
        play_address=be16(data, 12),
        songs=be16(data, 14),
        start_song=be16(data, 16),
        flags=be16(data, 0x76) if len(data) >= 0x78 else 0,
        title=sid_text(0x16),
        author=sid_text(0x36),
        released=sid_text(0x56),
    )


def split_lengths(value: str) -> list[int]:
    parts = [p for p in re.split(r"[\s,]+", value.strip()) if p]
    if not parts:
        raise ValueError("length list is empty")
    return [parse_duration(part) for part in parts]


def parse_duration(value: str) -> int:
    fields = value.split(":")
    if len(fields) == 1:
        seconds = float(fields[0])
    elif len(fields) == 2:
        seconds = int(fields[0]) * 60 + float(fields[1])
    elif len(fields) == 3:
        seconds = int(fields[0]) * 3600 + int(fields[1]) * 60 + float(fields[2])
    else:
        raise ValueError(f"invalid duration: {value}")
    return max(1, int(math.ceil(seconds)))


def format_duration(seconds: int) -> str:
    minutes, secs = divmod(seconds, 60)
    return f"{minutes}:{secs:02d}"


def parse_tune_set(value: str | None) -> set[int]:
    if not value:
        return set()
    result: set[int] = set()
    for part in re.split(r"[\s,]+", value.strip()):
        if not part:
            continue
        if "-" in part:
            start, end = part.split("-", 1)
            lo = int(start)
            hi = int(end)
            if hi < lo:
                raise ValueError(f"invalid tune range: {part}")
            result.update(range(lo, hi + 1))
        else:
            result.add(int(part))
    return result


def clean_ascii(value: str) -> str:
    return value.encode("ascii", errors="replace").decode("ascii")


def quote_sidscore(value: str) -> str:
    return clean_ascii(value).replace("\\", "\\\\").replace('"', '\\"')


def sid_id(value: str) -> str:
    words = re.findall(r"[A-Za-z0-9]+", value)
    if not words:
        return "Sid"
    ident = "".join(word[:1].upper() + word[1:] for word in words)
    if ident[0].isdigit():
        ident = "Sid" + ident
    return ident


def raw_md5(path: Path) -> str:
    return hashlib.md5(path.read_bytes()).hexdigest()


def find_hvsc_match(sid_path: Path, hvsc_root: Path) -> str | None:
    root = hvsc_root.resolve()
    try:
        return "/" + sid_path.resolve().relative_to(root).as_posix()
    except ValueError:
        pass

    target_md5 = raw_md5(sid_path)
    matches: list[str] = []
    for candidate in sorted(root.rglob("*.sid")):
        try:
            if raw_md5(candidate) == target_md5:
                matches.append("/" + candidate.resolve().relative_to(root).as_posix())
        except OSError:
            continue
    return matches[0] if matches else None


def read_hvsc_songlengths(songlengths_path: Path, relative_path: str) -> list[int] | None:
    if not songlengths_path.exists():
        return None
    current_path: str | None = None
    for raw_line in songlengths_path.read_text(encoding="latin-1", errors="replace").splitlines():
        line = raw_line.strip()
        if line.startswith(";"):
            comment = line[1:].strip()
            current_path = comment if comment.startswith("/") else None
            continue
        if current_path == relative_path and "=" in line:
            _digest, lengths = line.split("=", 1)
            return split_lengths(lengths)
    return None


def read_hvsc_stil(stil_path: Path, relative_path: str) -> list[str]:
    if not stil_path.exists():
        return []
    lines = stil_path.read_text(encoding="latin-1", errors="replace").splitlines()
    out: list[str] = []
    capture = False
    for raw_line in lines:
        line = raw_line.rstrip("\n")
        stripped = line.strip()
        if stripped == relative_path:
            capture = True
            continue
        if capture:
            if stripped.startswith("/") or stripped.startswith("###"):
                break
            if stripped:
                out.append(clean_ascii(stripped))
            elif out:
                break
    return out


def read_hvsc_metadata(sid_path: Path, hvsc_root: Path | None) -> HvscMetadata:
    if hvsc_root is None or not hvsc_root.exists():
        return HvscMetadata(None, None, [])
    relative_path = find_hvsc_match(sid_path, hvsc_root)
    if not relative_path:
        return HvscMetadata(None, None, [])
    docs = hvsc_root / "DOCUMENTS"
    lengths = read_hvsc_songlengths(docs / "Songlengths.txt", relative_path)
    stil_lines = read_hvsc_stil(docs / "STIL.txt", relative_path)
    return HvscMetadata(relative_path, lengths, stil_lines)


def run_siddump(siddump_path: Path, sid_path: Path, tune: int, dump_seconds: int, system: str,
        grid_frames: int, lowres: bool) -> str:
    command = [
        sys.executable,
        str(siddump_path),
        str(sid_path),
        f"-a{tune - 1}",
        f"-t{dump_seconds}",
        f"-c{middle_c_register(system):04X}",
    ]
    if lowres:
        command.extend([f"-n{grid_frames}", "-l"])
    result = subprocess.run(command, check=False, text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if result.returncode != 0:
        detail = result.stderr.strip() or result.stdout.strip()
        raise RuntimeError(f"siddump failed for subtune {tune}: {detail}")
    return result.stdout


def is_hex_token(token: str, width: int) -> bool:
    return bool(re.fullmatch(rf"[0-9A-Fa-f]{{{width}}}", token))


def parse_voice_segment(segment: str) -> VoiceState:
    tokens = segment.strip().split()
    if len(tokens) < 4:
        return VoiceState()
    freq_token = tokens[0]
    wave_token = tokens[-3]
    adsr_token = tokens[-2]
    pulse_token = tokens[-1]
    note_tokens = tokens[1:-3]

    note = None
    if note_tokens:
        raw_note = note_tokens[0].strip("()")
        if raw_note != "...":
            note = raw_note

    return VoiceState(
        freq=int(freq_token, 16) if is_hex_token(freq_token, 4) else None,
        note=note,
        wave=int(wave_token, 16) if is_hex_token(wave_token, 2) else None,
        adsr=tuple(int(c, 16) for c in adsr_token) if is_hex_token(adsr_token, 4) else None,
        pulse=int(pulse_token, 16) if is_hex_token(pulse_token, 3) else None,
    )


def parse_filter_segment(segment: str) -> FilterState:
    tokens = segment.strip().split()
    if len(tokens) < 4:
        return FilterState()
    cutoff_token, ctrl_token, mode_token, volume_token = tokens[:4]
    cutoff = None
    if is_hex_token(cutoff_token, 4):
        # siddump prints cutoff as (D415 << 5) | (D416 << 8); SIDScore uses 0..2047.
        cutoff = min(0x07FF, int(cutoff_token, 16) >> 5)
    return FilterState(
        cutoff=cutoff,
        res=int(ctrl_token[0], 16) if is_hex_token(ctrl_token, 2) else None,
        route=int(ctrl_token[1], 16) if is_hex_token(ctrl_token, 2) else None,
        mode=FILTER_NAMES.get(mode_token) if mode_token != "..." else None,
        volume=int(volume_token, 16) if is_hex_token(volume_token, 1) else None,
    )


def parse_dump(text: str) -> list[DumpRow]:
    rows: list[DumpRow] = []
    for line in text.splitlines():
        if not line.startswith("|"):
            continue
        parts = line.split("|")
        if len(parts) < 7:
            continue
        frame_text = parts[1].strip()
        if not frame_text.isdigit():
            continue
        rows.append(DumpRow(
            frame=int(frame_text),
            voices=[parse_voice_segment(parts[2 + index]) for index in range(VOICE_COUNT)],
            filter=parse_filter_segment(parts[5]),
        ))
    return rows


def state_rows(rows: list[DumpRow]) -> list[DumpRow]:
    voice_state = [VoiceState() for _ in range(VOICE_COUNT)]
    filter_state = FilterState()
    out: list[DumpRow] = []
    for row in rows:
        voices: list[VoiceState] = []
        for index, delta in enumerate(row.voices):
            current = voice_state[index]
            if delta.freq is not None:
                current.freq = delta.freq
            if delta.note is not None:
                current.note = delta.note
            if delta.wave is not None:
                current.wave = delta.wave
            if delta.adsr is not None:
                current.adsr = delta.adsr
            if delta.pulse is not None:
                current.pulse = delta.pulse
            voices.append(VoiceState(current.freq, current.note, current.wave, current.adsr, current.pulse))

        if row.filter.cutoff is not None:
            filter_state.cutoff = row.filter.cutoff
        if row.filter.res is not None:
            filter_state.res = row.filter.res
        if row.filter.route is not None:
            filter_state.route = row.filter.route
        if row.filter.mode is not None:
            filter_state.mode = row.filter.mode
        if row.filter.volume is not None:
            filter_state.volume = row.filter.volume

        out.append(DumpRow(
            frame=row.frame,
            voices=voices,
            filter=FilterState(filter_state.cutoff, filter_state.res, filter_state.route,
                               filter_state.mode, filter_state.volume),
        ))
    return out


def wave_name(wave: int | None) -> str | None:
    if wave is None:
        return None
    names = [name for bit, name in WAVE_BITS if wave & bit]
    return "+".join(names) if names else None


def has_gate(wave: int | None) -> bool:
    return wave is not None and bool(wave & 0x01)


def has_noise(wave: int | None) -> bool:
    return wave is not None and bool(wave & 0x80)


def choose_instrument(rows: list[DumpRow], voice: int) -> VoiceState:
    chosen: VoiceState | None = None
    fallback: VoiceState | None = None
    for row in state_rows(rows):
        state = row.voices[voice - 1]
        if state.wave is not None and wave_name(state.wave):
            fallback = fallback or state
            if has_gate(state.wave):
                chosen = state
                break
    result = chosen or fallback or VoiceState(wave=0x10, adsr=(0, 0, 0, 0), pulse=0)
    if result.adsr is None:
        result.adsr = (0, 0, 0, 0)
    return result


def note_to_sidscore(note: str | None) -> str | None:
    if not note:
        return None
    match = re.fullmatch(r"([A-G])([#-]?)(-?\d+)", note)
    if not match:
        return None
    name = match.group(1) + ("#" if match.group(2) == "#" else "")
    octave = int(match.group(3))
    return f"O{octave} {name}"


def notation_for_voice(rows: list[DumpRow], voice: int, length_frames: int) -> list[str]:
    events: list[str] = []
    last_note: str | None = None
    for row in state_rows(rows):
        if row.frame >= length_frames:
            break
        state = row.voices[voice - 1]
        converted = note_to_sidscore(state.note)
        if converted:
            last_note = converted
        if state.wave is None or not has_gate(state.wave) or not last_note:
            events.append("R8")
        else:
            events.append(f"{last_note}8")
    return events


def emit_event_lines(events: list[str], indent: str, per_line: int = 12) -> list[str]:
    return [indent + " ".join(events[index:index + per_line])
            for index in range(0, len(events), per_line)]


def adsr_text(adsr: tuple[int, int, int, int] | None) -> str:
    a, d, s, r = adsr or (0, 0, 0, 0)
    return f"{a},{d},{s},{r}"


def hex_value(value: int, width: int = 4) -> str:
    return f"${value:0{width}X}"


def instrument_line(tune: int, voice: int, instrument: VoiceState) -> str:
    name = f"t{tune}_v{voice}"
    wave = wave_name(instrument.wave) or "TRI"
    parts = [f"INSTR {name}", f"WAVE={wave}", f"ADSR={adsr_text(instrument.adsr)}"]
    if "PULSE" in wave and instrument.pulse is not None:
        parts.append(f"PW={hex_value(instrument.pulse)}")
    parts.extend(["GATE=LEGATO", "GATEMIN=1"])
    return " ".join(parts)


def voice_change_lines(previous: VoiceState, current: VoiceState, at: int | None, indent: str) -> list[str]:
    changes: list[str] = []
    if current.wave != previous.wave and current.wave is not None:
        wave = wave_name(current.wave)
        if wave:
            changes.append(f"WAVE={wave}")
        changes.append("GATE=ON" if has_gate(current.wave) else "GATE=OFF")
    if current.adsr != previous.adsr and current.adsr is not None:
        changes.append(f"ADSR={adsr_text(current.adsr)}")
    if current.freq != previous.freq and current.freq is not None:
        changes.append(f"FREQ={hex_value(current.freq)}")
    if current.pulse != previous.pulse and current.pulse is not None:
        changes.append(f"PW={hex_value(current.pulse)}")
    return assignment_lines(changes, at, indent)


def filter_change_lines(previous: FilterState, current: FilterState, at: int | None, indent: str) -> list[str]:
    changes: list[str] = []
    if current.mode != previous.mode and current.mode is not None:
        changes.append(f"FILTER={current.mode}")
    if current.route != previous.route and current.route is not None:
        changes.append(f"FILTERROUTE={hex_value(current.route, 1)}")
    if current.cutoff != previous.cutoff and current.cutoff is not None:
        changes.append(f"CUTOFF={hex_value(current.cutoff)}")
    if current.res != previous.res and current.res is not None:
        changes.append(f"RES={current.res}")
    if current.volume != previous.volume and current.volume is not None:
        changes.append(f"VOLUME={current.volume}")
    return assignment_lines(changes, at, indent)


def assignment_lines(changes: list[str], at: int | None, indent: str) -> list[str]:
    if not changes:
        return []
    if at is None:
        return [indent + change for change in changes]
    if len(changes) == 1:
        return [indent + f"{changes[0]} @{at}"]
    return [indent + f"FRAME {at} {{"] + [indent + "  " + change for change in changes] + [indent + "}"]


def effect_for_voice(name: str, voice: int, rows: list[DumpRow], length_frames: int,
        indent: str, include_filter: bool) -> list[str]:
    states = state_rows(rows)
    initial_voice = states[0].voices[voice - 1] if states else VoiceState()
    initial_filter = states[0].filter if states else FilterState()
    for attr, default in (("freq", 0), ("wave", 0x10), ("adsr", (0, 0, 0, 0)), ("pulse", 0)):
        if getattr(initial_voice, attr) is None:
            setattr(initial_voice, attr, default)

    lines = [
        f"{indent}EFFECT {name} {{",
        f"{indent}  VOICE {voice}",
        f"{indent}  LENGTH {length_frames} TICKS",
        "",
    ]
    lines.extend(voice_change_lines(VoiceState(), initial_voice, None, indent + "  "))
    if include_filter:
        lines.extend(filter_change_lines(FilterState(), initial_filter, None, indent + "  "))

    previous_voice = VoiceState(
        initial_voice.freq, initial_voice.note, initial_voice.wave, initial_voice.adsr, initial_voice.pulse)
    previous_filter = FilterState(
        initial_filter.cutoff, initial_filter.res, initial_filter.route, initial_filter.mode, initial_filter.volume)

    for row in states[1:]:
        if row.frame >= length_frames:
            break
        current_voice = row.voices[voice - 1]
        lines.extend(voice_change_lines(previous_voice, current_voice, row.frame, indent + "  "))
        if include_filter:
            lines.extend(filter_change_lines(previous_filter, row.filter, row.frame, indent + "  "))
        previous_voice = VoiceState(
            current_voice.freq, current_voice.note, current_voice.wave, current_voice.adsr, current_voice.pulse)
        previous_filter = FilterState(
            row.filter.cutoff, row.filter.res, row.filter.route, row.filter.mode, row.filter.volume)

    if not (previous_voice.wave is not None and not has_gate(previous_voice.wave)):
        lines.append(f"{indent}  GATE=OFF @{max(0, length_frames - 1)}")
    lines.append(f"{indent}}}")
    return lines


def filter_has_data(rows: list[DumpRow], length_frames: int) -> bool:
    for row in state_rows(rows):
        if row.frame >= length_frames:
            break
        current = row.filter
        if any(value is not None for value in (
                current.cutoff, current.res, current.route, current.mode, current.volume)):
            return True
    return False


def effect_for_filter(name: str, rows: list[DumpRow], length_frames: int, indent: str) -> list[str]:
    states = state_rows(rows)
    initial_filter = states[0].filter if states else FilterState()
    lines = [
        f"{indent}EFFECT {name} {{",
        f"{indent}  VOICE ANY",
        f"{indent}  LENGTH {length_frames} TICKS",
        "",
    ]
    lines.extend(filter_change_lines(FilterState(), initial_filter, None, indent + "  "))
    previous_filter = FilterState(
        initial_filter.cutoff, initial_filter.res, initial_filter.route, initial_filter.mode, initial_filter.volume)

    for row in states[1:]:
        if row.frame >= length_frames:
            break
        lines.extend(filter_change_lines(previous_filter, row.filter, row.frame, indent + "  "))
        previous_filter = FilterState(
            row.filter.cutoff, row.filter.res, row.filter.route, row.filter.mode, row.filter.volume)

    lines.append(f"{indent}}}")
    return lines


def system_from_header(header: SidHeader, requested: str) -> str:
    if requested != "AUTO":
        return requested
    clock_bits = (header.flags >> 2) & 0x03
    if clock_bits == 0x02:
        return "NTSC"
    return "PAL"


def frame_rate(system: str) -> float:
    return RASTER_RATE_NTSC if system == "NTSC" else RASTER_RATE_PAL


def sid_clock(system: str) -> float:
    return SID_CLOCK_NTSC if system == "NTSC" else SID_CLOCK_PAL


def default_tempo(system: str, grid_frames: int) -> int:
    return int(round((30.0 * frame_rate(system)) / grid_frames))


def duration_frames(seconds: int, system: str) -> int:
    return max(1, int(round(seconds * frame_rate(system))))


def dump_seconds_for_frames(frames: int) -> int:
    # siddump.py always interprets -t as PAL-style seconds, i.e. seconds * 50 calls.
    return max(1, int(math.ceil(frames / PAL_FRAMES_PER_SECOND)))


def middle_c_register(system: str) -> int:
    c4_hz = 440.0 * math.pow(2.0, (60 - 69) / 12.0)
    return int(round(c4_hz * 16777216.0 / sid_clock(system)))


def resolve_lengths(header: SidHeader, explicit_lengths: list[int] | None,
        hvsc_lengths: list[int] | None, fallback_seconds: int) -> tuple[list[int], str]:
    if explicit_lengths:
        source = "command line"
        lengths = explicit_lengths
    elif hvsc_lengths:
        source = "HVSC Songlengths.txt"
        lengths = hvsc_lengths
    else:
        source = f"fallback {fallback_seconds}s per subtune"
        lengths = []
    resolved = []
    for index in range(header.songs):
        resolved.append(lengths[index] if index < len(lengths) else fallback_seconds)
    return resolved, source


def convert_sid(args: argparse.Namespace) -> None:
    sid_display_path = args.sid.as_posix()
    sid_path = args.sid.resolve()
    out_path = args.output.resolve() if args.output else sid_path.with_suffix(".sidscore")
    siddump_path = args.siddump.resolve()
    header = read_sid_header(sid_path)
    system = system_from_header(header, args.system)

    hvsc_root = None if args.no_hvsc else args.hvsc_root.expanduser().resolve()
    hvsc = read_hvsc_metadata(sid_path, hvsc_root)
    explicit_lengths = split_lengths(args.lengths) if args.lengths else None
    lengths, length_source = resolve_lengths(header, explicit_lengths, hvsc.lengths, args.seconds)
    effect_tunes = parse_tune_set(args.effect_tunes)
    if args.all_effects:
        effect_tunes.update(range(1, header.songs + 1))
    tempo = args.tempo if args.tempo else default_tempo(system, args.grid_frames)

    low_rows: dict[int, list[DumpRow]] = {}
    full_rows: dict[int, list[DumpRow]] = {}
    instruments: dict[tuple[int, int], VoiceState] = {}

    for tune in range(1, header.songs + 1):
        seconds = lengths[tune - 1]
        frames = duration_frames(seconds, system)
        dump_seconds = dump_seconds_for_frames(frames)
        low_rows[tune] = parse_dump(run_siddump(
            siddump_path, sid_path, tune, dump_seconds, system, args.grid_frames, lowres=True))
        full_rows[tune] = parse_dump(run_siddump(
            siddump_path, sid_path, tune, dump_seconds, system, args.grid_frames, lowres=False))
        for voice in range(1, VOICE_COUNT + 1):
            instruments[(tune, voice)] = choose_instrument(full_rows[tune], voice)

    base_id = sid_id(header.title or sid_path.stem)

    def tune_is_effect_only(tune: int) -> bool:
        return tune in effect_tunes or lengths[tune - 1] <= args.effect_threshold

    def voice_is_effect(tune: int, voice: int) -> bool:
        return tune_is_effect_only(tune) or has_noise(instruments[(tune, voice)].wave)

    lines: list[str] = [
        f"; {clean_ascii(header.title or sid_path.stem)} ({sid_display_path})",
        "; Generated by tools/sid2sidscore.py from tools/siddump.py register traces.",
        f"; Source SID: {header.magic} v{header.version}, load {hex_value(header.load_address)}, "
        f"init {hex_value(header.init_address)}, play {hex_value(header.play_address)}, "
        f"{header.songs} subtune{'s' if header.songs != 1 else ''}, default {header.start_song}, "
        f"flags {hex_value(header.flags)}.",
        f"; Length source: {length_source}; lengths: {' '.join(format_duration(v) for v in lengths)}.",
        f"; Conversion: {args.grid_frames}-frame notation grid, TEMPO {tempo}, "
        f"SYSTEM {system}, effect threshold <= {args.effect_threshold}s.",
    ]
    if hvsc.relative_path:
        lines.append(f"; HVSC path: {hvsc.relative_path}")
    for stil_line in hvsc.stil_lines[:6]:
        lines.append(f"; STIL: {stil_line}")
    lines.extend([
        "",
        f'TITLE "{quote_sidscore(header.title or sid_path.stem)}"',
        f'AUTHOR "{quote_sidscore(header.author or "Unknown")}"',
        f'RELEASED "{quote_sidscore(header.released or "Unknown")}"',
        "",
        f"TEMPO {tempo}",
        "TIME 4/4",
        f"SYSTEM {system}",
        "",
    ])

    for tune in range(1, header.songs + 1):
        for voice in range(1, VOICE_COUNT + 1):
            if not voice_is_effect(tune, voice):
                lines.append(instrument_line(tune, voice, instruments[(tune, voice)]))
    lines.append("")

    def emit_tune_body(tune: int, indent: str) -> list[str]:
        out: list[str] = []
        length_frames = duration_frames(lengths[tune - 1], system)
        filter_assigned = False
        has_effect_voice = any(voice_is_effect(tune, voice) for voice in range(1, VOICE_COUNT + 1))
        if not has_effect_voice and filter_has_data(full_rows[tune], length_frames):
            out.extend(effect_for_filter(f"{base_id}Subtune{tune}Filter", full_rows[tune], length_frames, indent))
            out.append("")
            filter_assigned = True
        for voice in range(1, VOICE_COUNT + 1):
            if voice_is_effect(tune, voice):
                effect_name = f"{base_id}Subtune{tune}Voice{voice}"
                if has_noise(instruments[(tune, voice)].wave):
                    effect_name += "Noise"
                out.extend(effect_for_voice(
                    effect_name,
                    voice,
                    full_rows[tune],
                    length_frames,
                    indent,
                    include_filter=not filter_assigned,
                ))
                filter_assigned = True
            else:
                out.append(f"{indent}VOICE {voice} t{tune}_v{voice}:")
                out.extend(emit_event_lines(notation_for_voice(low_rows[tune], voice, length_frames), indent + "  "))
            out.append("")
        while out and out[-1] == "":
            out.pop()
        return out

    lines.extend(emit_tune_body(1, ""))
    if header.songs > 1:
        lines.append("")
    for tune in range(2, header.songs + 1):
        suffix = " SFX" if tune_is_effect_only(tune) else ""
        lines.extend([
            f"TUNE {tune} {{",
            f'  TITLE "{quote_sidscore(header.title or sid_path.stem)} Subtune {tune}{suffix}"',
            f"  TEMPO {tempo}",
            f"  SYSTEM {system}",
            "",
        ])
        lines.extend(emit_tune_body(tune, "  "))
        lines.extend(["", "}"])
        if tune != header.songs:
            lines.append("")

    out_path.write_text("\n".join(lines) + "\n", encoding="ascii")
    print(out_path)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Convert a PSID/RSID file to deterministic SIDScore notation/effects.")
    parser.add_argument("sid", type=Path, help="Input .sid file")
    parser.add_argument("-o", "--output", type=Path,
        help="Output .sidscore path (default: input basename with .sidscore)")
    parser.add_argument("--siddump", type=Path,
        default=Path(__file__).resolve().with_name("siddump.py"),
        help="Path to siddump.py (default: sibling tools/siddump.py)")
    parser.add_argument("--lengths",
        help="Subtune lengths, for example '0:40 1:14 0:55' or '40,74,55'")
    parser.add_argument("--seconds", type=int, default=DEFAULT_SECONDS,
        help=f"Fallback seconds per subtune when no length source is available (default: {DEFAULT_SECONDS})")
    parser.add_argument("--hvsc-root", type=Path, default=Path("~/Music/C64music"),
        help="HVSC root used to find Songlengths.txt/STIL.txt by raw SID match")
    parser.add_argument("--no-hvsc", action="store_true",
        help="Do not inspect a local HVSC tree for lengths and STIL comments")
    parser.add_argument("--grid-frames", type=int, default=DEFAULT_GRID_FRAMES,
        help=f"PAL frames per emitted eighth note for notation voices (default: {DEFAULT_GRID_FRAMES})")
    parser.add_argument("--tempo", type=int,
        help="SIDScore tempo to write (default: derived from --grid-frames)")
    parser.add_argument("--system", choices=("AUTO", "PAL", "NTSC"), default="AUTO",
        help="Video system to write and use for timing/pitch calibration (default: AUTO from PSID flags)")
    parser.add_argument("--effect-threshold", type=int, default=DEFAULT_EFFECT_THRESHOLD_SECONDS,
        help="Treat subtunes with this length or shorter as EFFECT-only (default: 4)")
    parser.add_argument("--effect-tunes",
        help="Comma/space separated subtunes or ranges to force as EFFECT-only, for example '6,9-12'")
    parser.add_argument("--all-effects", action="store_true",
        help="Emit all voices in all subtunes as EFFECT timelines")
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    try:
        if args.grid_frames <= 0:
            raise ValueError("--grid-frames must be positive")
        if args.seconds <= 0:
            raise ValueError("--seconds must be positive")
        convert_sid(args)
    except Exception as exc:
        print(f"sid2sidscore: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
