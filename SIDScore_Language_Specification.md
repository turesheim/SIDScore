# SIDScore Language Specification  

Version: **0.1 (draft)**

## 1. Scope and Goals

This language is a **SID-specific, high-level score format** for Commodore 64 music.  
It is designed to:

- Be readable like traditional sheet music
- Map **directly and predictably** to SID hardware constraints
- Be easy to **translate from MML**
- Compile or interpret into a **simple SID player** (3 voices, IRQ-driven)

This specification defines **syntax and musical semantics**, not the player implementation.

## 2. File Structure

A source file should have the `*.sidscore` suffix and will consist of a sequence of **statements**, in arbitrary order unless otherwise noted.

Statement types:

- Metadata statements
- Wavetable table definitions (optional extension)
- Instrument definitions
- Timing state statements
- Voice blocks

Lines starting with `;` are comments and MUST be ignored.

Whitespace (spaces, tabs, newlines) is insignificant except where noted.

### 2.1 Numeric Notation

- Unprefixed integers are **decimal** (base 10).
- Hexadecimal values MUST be prefixed with `$` (for example `$0800`, `$0A00`).
- Bare hex-style values without `$` are invalid.

## 3. Metadata Statements

### 3.1 TITLE

```
TITLE "string"
```

- MAY appear at most once
- Informational only (used for SID metadata)
- String MUST be quoted

### 3.2 AUTHOR

```
AUTHOR "string"
```

- MAY appear at most once
- Informational only (used for SID metadata)
- String MUST be quoted

### 3.3 RELEASED

```
RELEASED "string"
```

- MAY appear at most once
- Informational only (used for SID metadata)
- String MUST be quoted

### 3.4 TEMPO

```
TEMPO <integer>
```

- MUST appear exactly once
- Unit: beats per minute (BPM)
- Valid range: 1–300 (implementation MAY restrict further)

Example:
```
TEMPO 120
```

### 3.5 TIME

```
TIME <numerator>/<denominator>
```

- OPTIONAL
- Informational only in v0.1
- Does not affect timing or bar enforcement

### 3.6 SYSTEM (PAL / NTSC)

```
SYSTEM PAL
SYSTEM NTSC
```

- OPTIONAL
- Selects the intended C64 video system for timing and pitch
- Affects the SID master clock used for frequency quantization and playback speed
- If omitted, the default is implementation-defined (RECOMMENDED: PAL)

Example:

```
SYSTEM PAL
```

## 4. Wavetable Format

Wavetables define short, per-frame parameter sequences for an instrument.
They are an optional extension in v0.1; parsers/players MAY ignore them
with a warning.

### 4.1 Syntax

```
TABLE <type> <name> {
  <step>
  ...
}
```

- `<type>` MUST be one of: `wave`, `pw`, `gate`, `pitch`, `filter`
- `<name>` MUST be a valid identifier: `[A-Za-z_][A-Za-z0-9_]*`
- Table names MUST be unique

### 4.2 Steps

```
<value> @N
<value> HOLD
<control> @N
<control> HOLD
LOOP
```

- `N` MUST be an integer >= 1
- `@N` durations are measured in player frames (PAL/NTSC; see Timing Model)
- `HOLD` (or `@HOLD`) holds the value until the note ends
- `LOOP` restarts the table from the first step; if omitted, the final value is
  held until the note ends
- `<control>` is only valid for `TABLE wave` (see Wave Table Controls below)

### 4.3 Values by type

- `wave`: sets the SID waveform bits for the voice on each step.  
  Values: `PULSE`, `SAW`, `TRI`, `NOISE` (combinations with `+` allowed).  
  **Legacy form:** a step may be just the waveform value (e.g., `SAW @2`).  
  **Control form:** a step may include multiple control fields (see below).
- `pw`: sets the SID pulse width on each step (useful for PWM animation).  
  Values: decimal `0–4095` or `$`-prefixed hex `$0000`–`$0FFF` (12-bit SID pulse width).  
  If the active waveform is not `PULSE`, the value MAY be ignored with a warning.
- `gate`: toggles the gate bit on each step, creating hard retriggers or stutters.  
  Values: `ON` or `OFF`.  
  `OFF` enters release; `ON` restarts the envelope (implementation-defined).
- `pitch`: applies a semitone offset to the note for each step (vibrato/glide).  
  Values: signed integers like `-12`, `-1`, `0`, `+1`, `+7`.  
  Offsets are applied to the note's base pitch before SID quantization.  
  For `NOISE` events, offsets are ignored (or MAY map to noise frequency).
- `filter`: sets the SID filter cutoff per step.  
  Values: integer `0–2047` (decimal, or `$`-prefixed hex).  
  Lower values are darker/more muffled, higher values are brighter.

#### 4.3.1 Wave Table Controls (SidTracker-style)

Wave steps may include multiple control fields. Any field omitted means **no change** for that parameter.

```
WAVE=<wave> [NOTE=<abs|rel|OFF>] [GATE=ON|OFF] [RING=ON|OFF] [SYNC=ON|OFF] [RESET]
```

**NOTE mode**

- **Fixed note**: `NOTE=C4` (absolute pitch). Digits are an **octave** in this context.
- **Relative note**: `NOTE=+7` or `NOTE=-5` (semitone offset relative to the current note base).
- **No change**: omit `NOTE` or use `NOTE=OFF`.

**GATE**

- `GATE=ON` keeps or reasserts the envelope (if gate was off).
- `GATE=OFF` forces release.

**RING**

- Enables ring modulation for the voice at that step.
- On real SID, triangle waveform is implicitly required for audible ring-mod output.
- Uses the fixed SID modulator pairing: V1 is modulated by V3, V2 by V1, V3 by V2.

**SYNC**

- Enables hard sync at that step.
- Syncs the oscillator to its fixed SID partner (same pairing as above).

**RESET**

- Forces oscillator reset at that step (phase reset).

### 4.4 Attachment to instruments

Wavetables are attached via optional instrument parameters:
`WAVESEQ=<name>`, `PWSEQ=<name>`, `GATESEQ=<name>`, `PITCHSEQ=<name>`, `FILTERSEQ=<name>`.

Rules:

- Tables reset at each new NOTE/NOISE event
- While a table is active, it drives the corresponding SID parameter
- Base `WAVE`/`PW` values act as defaults when no table is attached
- `PITCHSEQ` offsets the event's pitch; `0` means no change
- A note end always forces gate OFF (tables do not extend note length)

Example:

```
TABLE wave LeadWave {
  NOISE @1
  SAW   @1
  PULSE HOLD
}
TABLE wave LeadCtrl {
  WAVE=SAW NOTE=C4 GATE=ON @2
  NOTE=+7 RING=ON SYNC=ON @2
  NOTE=OFF GATE=OFF @1
  RESET @1
  LOOP
}
TABLE pw LeadPWM {
  $0600 @2
  $0800 @2
  $0A00 @2
  $0800 @2
  LOOP
}
TABLE pitch LeadVib {
  0   @1
  +1  @1
  0   @1
  -1  @1
  LOOP
}

INSTR lead WAVE=PULSE ADSR=8,2,10,4 WAVESEQ=LeadWave PWSEQ=LeadPWM PITCHSEQ=LeadVib
```

## 5. Instrument Definitions

### 5.1 Syntax

```
INSTR <name> WAVE=<wave> ADSR=a,d,s,r [PW=<int|$hex>] [SYNC=ON|OFF] [RING=ON|OFF]
            [FILTER=OFF|LP|BP|HP|LP+BP|LP+HP|BP+HP|LP+BP+HP] [CUTOFF=<int>] [RES=<int>]
            [GATE=RETRIGGER|LEGATO] [GATEMIN=<int>]
            [WAVESEQ=<name>] [PWSEQ=<name>] [GATESEQ=<name>] [PITCHSEQ=<name>] [FILTERSEQ=<name>]
```

- `<name>` MUST be a valid identifier: `[A-Za-z_][A-Za-z0-9_]*`
- Instrument names MUST be unique

**Import form (reuse an existing definition):**

```
INSTR <name> "path/to/instrument.sidscore"
```

Rules:

- The quoted string is a path to another SIDScore file.
- Relative paths are resolved from the importing file’s directory.
- The imported file MUST define an instrument with the same `<name>`.
- Only `INSTR` and `TABLE` statements from the imported file are processed; other statements are ignored.
- Duplicate instrument names (across inline or imported definitions) are compile errors.
- Import cycles MUST be detected and treated as compile errors.

Example:

```
INSTR harpsichord "harpsichord.sidscore"
```

### 5.2 Parameters

#### WAVE

Allowed values:
- `PULSE`
- `SAW`
- `TRI`
- `NOISE`
  
Multiple waveforms MAY be combined using `+` (e.g., `WAVE=SAW+PULSE`).

**What combinations sound like (rule of thumb):**

- `SAW+PULSE`: brighter and more "buzzy" than either alone.
- `TRI+PULSE`: hollow with a gentle edge; good for soft leads.
- `TRI+SAW`: smoother than saw but with extra presence.
- `NOISE+PULSE/SAW/TRI`: noisy, percussive, or gritty textures.

Note: On real SID hardware, combined waveforms are **not** simple sums. The
resulting spectrum is non‑linear and chip‑dependent. In this DSL’s reference
runtime, combinations are approximated by mixing the digital waveforms.

Example:

```
INSTR lead WAVE=SAW ADSR=6,4,10,4
```

Example (combined):

```
INSTR lead WAVE=SAW+PULSE ADSR=6,4,10,4 PW=$0800
```

#### ADSR

- Four decimal integers (0–15): Attack, Decay, Sustain, Release
- MUST map directly to SID ADSR nibbles
  
Plain-language: how fast the sound starts, how it falls, how loud it holds, and how it fades when released.

Example:

```
INSTR lead WAVE=TRI ADSR=8,2,10,4
```

#### GATE (default articulation)

- OPTIONAL, default `RETRIGGER`
- Defines how NOTE/NOISE events control the SID GATE bit when not in `(leg)` scope and not tied (`&`).

Values:

- `RETRIGGER`: Each new NOTE asserts a new attack by toggling GATE (OFF→ON) before sounding the note.
- `LEGATO`: Each new NOTE changes pitch without toggling GATE if the voice is already active.

Notes:

- Rests always force GATE OFF.
- Ties (`&`) always force legato between the tied notes, regardless of this setting.
- `(leg)` scope always forces legato across notes until `(end)` or a rest.
- This setting only affects **articulation** (whether the envelope is retriggered). It does **not** change note length.
- If a `GATESEQ` is attached, it can still toggle the GATE bit while the note is active; `GATE` only sets the
  default behavior when the note starts.

#### GATEMIN (minimum gate-on time)

- OPTIONAL, default `0`
- Integer number of player frames that GATE must remain ON once asserted.
- Intended to avoid clicks on very short notes; not part of musical time.
- When `GATEMIN` > 0, very short notes may sound slightly longer, because the gate cannot turn off until the
  minimum time has elapsed.
- `GATEMIN` applies even in `LEGATO` mode when the gate is asserted.

#### PW (Pulse Width)

- OPTIONAL
- Value `0–65535` (decimal) or `$`-prefixed hex (`$0`–`$FFFF`)
- Meaningful only for `PULSE`
- Using PW with other waveforms SHOULD emit a warning
  
Plain-language: changes the pulse wave's timbre from thin to hollow.

Example:

```
INSTR lead WAVE=PULSE ADSR=8,2,10,4 PW=$0800
```

#### PWM Sweep (PWMIN / PWMAX / PWSWEEP)

- OPTIONAL pulse width modulation parameters
- `PWMIN` / `PWMAX`: bounds `0–4095` (decimal) or `$`-prefixed hex (`$0000–$0FFF`)
- `PWSWEEP`: signed decimal delta applied each player frame
- Values are clamped at the min/max edges (no bounce)
- Ignored if waveform does not include `PULSE` (should warn)

Example:

```
INSTR pad WAVE=PULSE ADSR=6,4,10,4 PW=$0800 PWMIN=$0400 PWMAX=$0C00 PWSWEEP=+8
```

#### FILTER (routing + mode)

- OPTIONAL, default `OFF`
- Sets the SID filter mode for this instrument and routes the voice through the filter.
- Values: `LP`, `BP`, `HP`, or combinations with `+` (e.g., `LP+HP` for notch), or `OFF`.

Notes:
- The SID filter is **global** hardware. If multiple voices use different filter settings, the last update wins.
- The reference runtime applies a single global filter to all routed voices; the exported SID player mirrors this behavior.

Example:

```
INSTR lead WAVE=SAW ADSR=6,4,10,4 FILTER=LP
```

#### CUTOFF

- OPTIONAL, default `0`
- Integer `0–2047` (decimal; `$`-prefixed hex also allowed)
- Only meaningful when `FILTER` is not `OFF`

#### RES

- OPTIONAL, default `0`
- Integer `0–15` (SID resonance nibble)
- Only meaningful when `FILTER` is not `OFF`

#### FILTERSEQ

- OPTIONAL reference to a `TABLE filter` (see Wavetable Format)
- While active, the table overrides the current cutoff value
- Resets at each NOTE/NOISE event, like other `*SEQ` tables

### 5.3 Recommended Instrument Layout (Readable Style)

Whitespace is insignificant, so you can split an `INSTR` statement across multiple lines.
The following layout makes it easier to copy settings from trackers like SidTracker64 by
grouping related parameters.

```
; --- Waveform ---
TABLE wave LeadWave {
  WAVE=SAW   @2
  WAVE=PULSE @2
  WAVE=TRI   HOLD
}

; --- Volume Envelope ---
; ADSR=6,4,10,4

; --- Vibrato (Pitch modulation) ---
TABLE pitch LeadVib {
  0   @3
  +1  @1
  0   @3
  -1  @1
  LOOP
}

; --- Pulse Modulation (PWM) ---
TABLE pw LeadPWM {
  $0600 @2
  $0800 @2
  $0A00 @2
  $0800 @2
  LOOP
}

; --- Filter Modulation ---
TABLE filter LeadCut {
  $0400 @2
  $0600 @2
  $0700 @2
  $0600 @2
  LOOP
}

; --- Instrument ---
INSTR lead
  ; Waveform
  WAVE=SAW+PULSE
  WAVESEQ=LeadWave

  ; Volume Envelope
  ADSR=6,4,10,4

  ; Vibrato
  PITCHSEQ=LeadVib

  ; Pulse Modulation
  PWSEQ=LeadPWM

  ; Filter Modulation
  FILTER=LP
  CUTOFF=600
  RES=8
  FILTERSEQ=LeadCut

  ; Articulation
  GATE=RETRIGGER
  GATEMIN=1
```

Notes:

- **Waveform** settings map to `WAVE`, `WAVESEQ`.
- **Volume Envelope** maps to `ADSR`.
- **Vibrato** maps to `PITCHSEQ` (small semitone offsets).
- **Pulse Modulation** maps to `PW`, `PWMIN`, `PWMAX`, `PWSWEEP`, or `PWSEQ`.
- **Articulation** maps to `GATE`, `GATEMIN`, `GATESEQ`.
- **Filter Modulation** maps to `FILTER`, `CUTOFF`, `RES`, `FILTERSEQ`.

### 5.4 SidTracker64 Copying Guide (Informative)

If you are transcribing a SidTracker64 instrument, this mapping is a practical starting point:

- **Waveform / Wavetable** → `WAVE` + optional `WAVESEQ`
- **Volume Envelope** → `ADSR` (SIDScore has one ADSR per instrument)
- **Vibrato** → `PITCHSEQ` (small offsets like `+1`, `-1`, with short durations)
- **PWM sweep / PWM table** → `PWSWEEP` or `PWSEQ` (plus `PWMIN`/`PWMAX`)
- **Hard sync / Ring mod** → `SYNC=ON`, `RING=ON`
- **Hard restart** → not available (SIDScore does not expose oscillator reset)
- **Filter (cutoff/mode/resonance)** → `FILTER`, `CUTOFF`, `RES`, `FILTERSEQ`

Tip: Start with the readable layout in §5.3 and fill in each section. Anything not supported should be
commented out until a future version adds it.

#### WAVESEQ / PWSEQ / GATESEQ / PITCHSEQ / FILTERSEQ

- OPTIONAL references to wavetable names (see Wavetable Format)
- `WAVESEQ` overrides the waveform while active, and may also apply NOTE/GATE/RING/SYNC/RESET controls per step
- `GATESEQ` toggles gate ON/OFF during a note; note end still forces gate OFF
- `PITCHSEQ` applies per-step semitone offsets to the event pitch
- `FILTERSEQ` overrides the cutoff while active (only if `FILTER` is enabled)

Notes (current reference runtime):
- `GATESEQ` ON after OFF retriggers the envelope attack
- `GATESEQ` OFF enters release while the note continues


##### Wave type tables - WAVESEQ

- Per‑note wavetable override: On each NOTE (and NOISE) event, the wave table resets to step 0. While the note is active, the table drives the waveform bits in the SID control register.
- Frame‑based timing: Each step duration is in player frames (PAL/NTSC). HOLD (or duration 0) holds the current waveform until the note ends. LOOP repeats from the first step.
- Overrides WAVE: The instrument’s base WAVE= is the default. If WAVESEQ= is attached, it replaces the active waveform bits while the note plays.
- Gate/sync/ring preserved: Only the waveform bits change. Gate, sync, and ring‑mod bits stay as defined by the current event/instrument.

  Example:

```
  TABLE wave LeadWave {
    SAW   @2
    PULSE @2
    TRI   @2
    PULSE @2
    LOOP
  }
  INSTR lead WAVE=PULSE ADSR=8,2,10,4 WAVESEQ=LeadWave
```

So the note plays with SAW for 2 frames, then PULSE for 2, TRI for 2, then back to PULSE for 2, repeating until the note ends.

Notes:

- Combined waveforms like SAW+PULSE are allowed; realtime uses a digital mix approximation, SID output uses the raw bit combination.
- WAVESEQ doesn’t affect pitch or gate timing; it’s purely the waveform bits.

##### Pulse width tables - PWSEQ

PWSEQ drives the pulse width over time while a note is playing.

  - Per‑note reset: The PW table resets to step 0 at the start of each NOTE (and NOISE) event.
  - Frame‑based timing: Step durations are in player frames (PAL/NTSC).
    HOLD (or duration 0) holds the current PW until the note ends.
    LOOP repeats from the first step.
  - Overrides base PW/PWM: While the PW table is active, it replaces the pulse width. The base PW=,
    PWMIN/PWMAX, and PWSWEEP are ignored during table playback.
  - Clamped to 12‑bit: Values are clamped to `0..4095` (`$0000..$0FFF`).

  Example:

```
  TABLE pw PulseWobble {
    $0600 @2
    $0800 @2
    $0A00 @2
    $0800 @2
    LOOP
  }

  INSTR lead WAVE=PULSE ADSR=8,2,10,4 PW=$0800 PWSEQ=PulseWobble
```

This will cycle the pulse width through those values for as long as the note sustains, producing PWM
  movement.

#### SYNC

- OPTIONAL, default `OFF`
- When `ON`, this voice's oscillator is hard‑synced to its modulator voice
- Modulator mapping (SID convention):  
  - VOICE 1 syncs to VOICE 3  
  - VOICE 2 syncs to VOICE 1  
  - VOICE 3 syncs to VOICE 2
  
Plain-language: each time the modulator voice crosses a phase threshold, this voice restarts its waveform.

Example:

```
INSTR syncLead WAVE=SAW SYNC=ON ADSR=6,2,10,4
```

#### RING

- OPTIONAL, default `OFF`
- When `ON`, this voice's triangle wave is ring‑modulated by the modulator voice
- Modulator mapping is the same as SYNC
  
Plain-language: the triangle wave is flipped by the other voice's phase, creating a metallic tone.

Example:

```
INSTR ringLead WAVE=TRI RING=ON ADSR=4,3,10,4
```

## 6. Voice Model (SID Mapping)

- There are up to **three voices** (VOICE 1–3)
- Each defined voice maps **1:1** to a SID voice
- Omitted voices are silent

```
VOICE <1|2|3> <instrument> :
```

Rules:
- Voice index MUST be 1, 2, or 3
- `<instrument>` MUST reference a defined INSTR
- Instrument changes inside a voice body are **not allowed** in v0.1

## 7. Voice Body Syntax

A voice body is a **linear sequence of tokens** evaluated left-to-right.

### 7.1 Voice-Local State

#### Octave

```
O<n>     ; set octave
>        ; octave +1
<        ; octave -1
```

- Valid octave range: implementation-defined (RECOMMENDED 0–7)
- Values outside range MAY be clamped with warning
  
Plain-language: `O4` means "middle" pitch range; `>` goes higher, `<` goes lower.

Example:

```
O4 C D E  > C  < B A G
```

#### Default Note Length

```
L<n>
```

Allowed values:
- 1, 2, 4, 8, 16, 32, 64

The default length applies until changed.

Example:

```
L8  C D E F  L4  G A
```

### 7.2 Notes and Rests

#### Notes

```
C D E F G A B
```

- Optional accidental: `#` or `b`
- Optional explicit length: `C4`, `D8`
- Optional dotted suffix: `.`

If no length is given, the current `L` applies.
  
Plain-language: `C` is a note, `C8` is a shorter C, `C8.` is a dotted (longer) C.

Example:
```
C8. R16  D  E4
```

#### Rests

```
R
R4
R8.
```

Semantics identical to notes, but silence.

## 8. Tie and Legato

### 8.1 Tie (`&`)

```
G & G
```

Semantics:
- Joins two consecutive notes into a **single gate**
- Gate is not retriggered
- Durations are summed
  
**Plain‑language**: it turns two identical notes into one longer note, so the sound does not restart.

Example:

```
G & G
```

Rules (v0.1):
- `&` MUST appear between two notes
- Both notes MUST have identical pitch
- `D & C` is a **compile error**

### 8.2 Legato Scope

```
(leg)
  A B >C D
(end)
```

Semantics:
- Gate is triggered on first note
- Gate remains held across all following notes
- Pitch may change freely
- Rests inside `(leg)` terminate the gate
  
**Plain‑language**: play a smooth phrase where each new note changes pitch but the sound stays “connected.”

Example:

```
(leg) C D E F (end)
```

Rules:
- Nested `(leg)` scopes are NOT allowed
- `(leg)` MUST be closed by `(end)`

## 9. Tuplets (Trioles)

### 9.1 Syntax

```
T{ E F G }
```

### 9.2 Semantics

- Represents **3 events in the time of 2**
- Based on the **current default length `L`**
- Example:
  - If `L8`, then `T{...}` occupies the time of **two 8th notes**
  
**Plain‑language**: squeeze three notes into the time where you would normally fit two.

Example:

```
L8  T{ C D E }
```

Rules (v0.1):
- `T{}` MUST contain exactly **three logical note/rest events**
- Ties inside a triol are allowed and count as one event
- Nested `T{}` scopes are NOT allowed
- Swing does NOT apply inside `T{}`

## 10. Swing

### 10.1 Syntax

```
SWING <percent>
SWING OFF
```

- `<percent>` is an integer (recommended range 50–75)
- 50 = straight timing

### 10.2 Semantics

- Swing applies to **pairs of notes at the current rhythmic level**
- Intended primarily for 8th notes (`L8`)
- Affects duration ratio of first/second note in a pair
  
**Plain‑language**: instead of two evenly spaced notes, the first is longer and the second is shorter (a “long‑short” feel).

Example:

```
SWING 60
L8 C D E F
```

Rules:
- Swing is **voice-local**
- Top-level SWING sets default for all voices
- Swing is ignored inside `T{}`

## 11. Repetition

### 11.1 Syntax

```
( ... )xN
```

- `N` MUST be an integer ≥ 1
- Repetition applies to the enclosed token sequence

Rules:
- Nested repeats are allowed
- Repeat scopes MUST close within the same VOICE
- `x1` is legal and repeats the body once

## 12. Noise Voice and Drum Tokens

### 12.1 `X` Token

```
X
X8
X8.
```

Semantics:
- Triggers a noise hit
- Duration follows same rules as notes

Rules:
- `X` is only legal if the instrument waveform includes `NOISE`
- Using `X` in a non-NOISE voice is a compile error

Example:

```
INSTR drum WAVE=NOISE ADSR=0,9,3,0
VOICE 3 drum: X8  R8  X8
```

### 12.2 Notes in NOISE Voice

- Notes (`C D E ...`) in a NOISE voice:
  - SHOULD emit a warning
  - Pitch MAY be ignored or mapped to noise frequency (implementation-defined)

## 13. Timing Model (Compiler / Player Contract)

- Musical durations are expressed in note values
- Compiler MUST convert durations into an internal tick unit
- RECOMMENDED internal resolution: **1/192 whole note**
- Swing and dotted notes MUST be resolved at compile time
- Player MAY operate at fixed frame rate (PAL 50 Hz / NTSC 60 Hz)

## 14. Errors and Diagnostics

### Compile Errors (MUST fail)

- Undefined instrument reference
- Undefined wavetable reference (if wavetables are supported)
- Missing instrument import file
- Imported file does not define the requested instrument name
- Import cycles
- Invalid voice index
- Unbalanced scopes: `(leg)/(end)`, `T{}`, `( )xN`
- Illegal tie usage
- Illegal token for voice type
- Invalid triol structure

### Warnings (SHOULD emit)

- Pulse width on non-PULSE waveform
- Notes in NOISE voice
- Out-of-range octave or parameters

## 15. Non-Goals (v0.1)

- Polyphony within a voice
- Automatic voice stealing
- Filters and modulation lanes beyond the wavetable sequences described here
- Instrument changes mid-voice
- Microtonal tuning

## 16. Design Principle

**What you write must be something the SID can actually do.**  
No implicit magic, no hidden voices, no invisible retriggers.

## 17. Glossary

### 17.1 Quick Musical Glossary

- **Beat**: the steady “pulse” you tap your foot to.  
- **Tempo (BPM)**: how fast the beat is (beats per minute).  
- **Note length**: how long a note lasts (e.g., quarter note, eighth note).  
- **Octave**: the same note name higher or lower in pitch (C4 is middle C).  
- **Dotted note**: adds half of the note's length (e.g., an 8th dot = 8th + 16th).  
- **Rest**: silence for a specific length (same lengths as notes).  
- **Tie (`&`)**: connect two identical notes so they sound as one longer note.  
- **Legato**: notes flow into each other without re‑starting the sound each time.  
- **Swing**: pairs of notes are played “long‑short” instead of perfectly even.  
- **Tuplet / Triol (`T{ ... }`)**: fit three notes into the time usually used by two.

### 17.2 Quick SID Glossary

- **SID**: the C64 sound chip. It has 3 hardware voices and simple waveforms.  
- **Voice**: one independent sound channel on the SID (three total).  
- **Waveform**: the basic shape of the sound (PULSE, SAW, TRI, NOISE).  
- **Pulse width (PW)**: the shape of a pulse wave; changes its tone.  
- **ADSR**: how a sound evolves over time: Attack (rise), Decay (fall), Sustain (hold), Release (fade).  
- **Gate**: whether the sound is currently "on" (note sounding) or "off".  
- **Noise**: a random-ish waveform used for drums/percussion.  
- **Sync**: a voice resets its oscillator when a paired voice crosses a threshold (hard sync).  
- **Ring mod**: a voice's triangle wave is flipped by another voice's phase, creating metallic timbres.  
- **PAL/NTSC**: video standards with different clocks; this affects pitch and timing on real hardware.  
- **Wavetable**: a small per-frame sequence of parameter values used to animate a sound.  
