# SIDScore Player Server Specification

Version: **0.1.0 (draft)**

## 1. Purpose

The SIDScore Player Server is a server that plays `.sidscore` files through SRAP
and streams compact binary playback data to an IDE client, primarily the 
Commodore Commander.

The server keeps SIDScore parsing, timing, playback, and source mapping in the
Java implementation. The IDE renderscontrols, voice visualizers, waveform 
scopes, and editor highlights.

This specification covers:

- Launch and connection model
- Binary frame protocol
- Playback commands
- Voice and waveform telemetry
- Score source maps for editor highlighting
- Live highlight state

The protocol does not expose MIDI as a public concept. Pitch is reported as 
SIDScore note display data and SID frequency register values.

## 2. Process Model

The Commodore Commander backend starts the server as a child process:

```sh
java -jar sidscore-cli.jar --player-server --port 0
```

The server binds to `127.0.0.1` only. Port `0` means the operating system chooses a
free port.

After the server socket is ready, the process writes one UTF-8 JSON line to
stdout:

```json
{"event":"ready","protocol":"srap-server","version":1,"port":51234}
```

Commodore Commander reads this line, opens a TCP connection to `127.0.0.1:<port>`,
and then uses the binary protocol described below. Stdout is only for bootstrap
and diagnostics.

The server exits when stdin closes, the parent process terminates it, or a fatal
startup error occurs.

## 3. Connection Model

The first version uses one full-duplex TCP connection:

- Client to server: playback commands.
- Server to client: playback state, score maps, voice state, scope buckets,
  highlight state, and errors.

Only one client connection is required. A server MAY reject additional clients
with an error frame or by closing the connection.

All TCP sockets MUST bind to loopback (`127.0.0.1`). The server MUST NOT bind to
`0.0.0.0` unless a future explicit remote mode is added.

## 4. Framing

All binary frames use little-endian byte order.

Every frame has this fixed header:

```text
u32 magic            0x53524150 ASCII "SRAP"
u8  version          protocol version, currently 1
u8  type             frame type
u16 flags            type-specific flags, otherwise 0
u32 sequence         sender-local monotonically increasing sequence number
u64 timestampNanos   sender monotonic time, normally System.nanoTime()
u32 payloadLength    number of bytes after this header
u8[payloadLength] payload
```

Frames with an unknown `version` MUST be rejected. Frames with an unknown
`type` MAY be ignored if the receiver can safely skip `payloadLength` bytes.

Maximum payload size for version 1 is 4 MiB. Larger frames MUST be rejected with
`ERROR` and the connection SHOULD be closed.

## 5. Primitive Types

```text
u8, i8       8-bit unsigned/signed integer
u16, i16     16-bit unsigned/signed integer
u32, i32     32-bit unsigned/signed integer
u64, i64     64-bit unsigned/signed integer
f32          IEEE 754 32-bit float
bool8        u8, 0=false, non-zero=true
str          u16 byteLength followed by UTF-8 bytes
```

Strings are UTF-8 and are not null-terminated.

Line and column positions are 1-based. Columns are UTF-16 code-unit columns so
they map directly to Monaco/Theia editor positions.

## 6. Frame Types

```text
0x01 HELLO              client -> server
0x02 HELLO_ACK          server -> client

0x10 PLAY               client -> server
0x11 PAUSE              client -> server
0x12 CONTINUE           client -> server
0x13 STOP               client -> server

0x20 PLAYBACK_STATE     server -> client
0x21 SCORE_MAP          server -> client
0x22 HIGHLIGHT_STATE    server -> client
0x23 VOICE_STATE        server -> client
0x24 SCOPE_BUCKETS      server -> client

0x7f ERROR              both directions
```

## 7. Handshake

The client MUST send `HELLO` first.

### HELLO Payload

```text
str clientName
u16 minVersion
u16 maxVersion
u32 clientCapabilities
```

Version 1 capabilities:

```text
bit 0  wants SCORE_MAP
bit 1  wants HIGHLIGHT_STATE
bit 2  wants VOICE_STATE
bit 3  wants SCOPE_BUCKETS
```

### HELLO_ACK Payload

```text
u16 selectedVersion
u32 serverCapabilities
str serverName
```

If no compatible version exists, the server sends `ERROR` and closes the
connection.

## 8. Playback Commands

### PLAY Payload

```text
u32 requestId
str sourceUri
str sourcePath
u8  sidModel          0=default, 1=6581, 2=8580
u8  reserved[3]
```

`sourceUri` is the IDE-facing URI. `sourcePath` is the local filesystem path the
Java process can read.

On successful `PLAY`, the server parses and resolves the score, emits
`PLAYBACK_STATE` with state `loading`, emits `SCORE_MAP` if requested, then
starts playback and emits `PLAYBACK_STATE` with state `playing`.

If another score is already playing, `PLAY` stops the current score and starts
the new one from the beginning.

### PAUSE Payload

```text
u32 requestId
```

`PAUSE` pauses audio and telemetry progression without discarding player state.
The server responds with `PLAYBACK_STATE` state `paused`.

### CONTINUE Payload

```text
u32 requestId
```

`CONTINUE` resumes from `paused`. It does not restart a stopped score.

If no score is paused, the server responds with `ERROR` code
`invalid_state`.

### STOP Payload

```text
u32 requestId
```

`STOP` stops playback, silences all voices, clears active highlight ids, and
resets the current playback position. The loaded score map MAY remain cached,
but there is no resumable audio state after stop.

After `STOP`, the server emits:

- `HIGHLIGHT_STATE` with all active event ids set to `-1`
- `PLAYBACK_STATE` state `stopped`

## 9. Playback State

### PLAYBACK_STATE Payload

```text
u32 requestId          0 for unsolicited state changes
u8  state
u8  reason
u16 reserved
u64 scoreId
u64 frameIndex
u64 elapsedNanos
```

States:

```text
0 idle
1 loading
2 playing
3 paused
4 stopped
5 ended
6 error
```

Reasons:

```text
0 none
1 client_request
2 end_of_score
3 parse_error
4 playback_error
5 connection_closed
```

`scoreId` is generated by the server for each successful `PLAY`.

## 10. Score Map

The score map is sent once after a successful parse and before playback starts.
It maps compiled timeline events to source ranges. The IDE uses this for editor
highlighting.

A score map is static for one `scoreId`. Live highlight frames only reference
event ids from this map.

### SCORE_MAP Payload

```text
u64 scoreId
u16 sourceCount
repeat sourceCount:
  u16 sourceId
  str sourceUri
  str sourcePath

u32 eventCount
repeat eventCount:
  i32 eventId
  u8  voiceIndex       1..3
  u8  noteKind
  u16 flags
  u64 startFrame
  u64 endFrame
  u16 sourceId
  u32 startLine
  u32 startColumn
  u32 endLine
  u32 endColumn
  str displayText
```

`eventId` values are unique within one `scoreId`.

`noteKind`:

```text
0 none/rest
1 note
2 noise
```

Event flags:

```text
bit 0 gate starts on this event
bit 1 legato or held from previous note
bit 2 retrigger gap event
bit 3 generated from repeated source range
bit 4 source range is approximate
```

For repeats, tuplets, and expanded structures, several timeline events MAY point
to the same source range. Each still gets its own `eventId`.

For imported files, `sourceId` references that imported source URI/path.

## 11. Highlight State

Highlight state is sent during playback at frame-event boundaries and MAY also
be sent once per telemetry block for simplicity.

### HIGHLIGHT_STATE Payload

```text
u64 scoreId
u64 frameIndex
i32 activeEventVoice1
i32 activeEventVoice2
i32 activeEventVoice3
```

`-1` means no active highlight for that voice.

The IDE highlights the source ranges for the active event ids in the current
`SCORE_MAP`.

The Java server is authoritative for highlight timing. The IDE MUST NOT attempt
to re-expand repeats, tuplets, ties, gate-min behavior, or imports.

## 12. Voice State

Voice state reports SIDScore/SID playback state for visualizers. It does not
expose MIDI.

### VOICE_STATE Payload

```text
u64 scoreId
u64 blockIndex
u64 frameIndex
f32 sampleRate

repeat 3 voices:
  u8  voiceIndex       1..3
  u8  noteKind
  u8  noteLetter
  i8  accidental
  i8  octave
  u8  waveMask
  u16 flags
  u16 freqReg
  u16 pulseWidth
  i8  pitchOffsetSemitones
  u8  reserved[3]
  f32 envelopeLevel
  f32 outputLevel
```

`noteKind`:

```text
0 none/rest
1 note
2 noise
```

`noteLetter`:

```text
0 C
1 D
2 E
3 F
4 G
5 A
6 B
255 none/noise
```

`accidental`:

```text
-1 flat
 0 natural
 1 sharp
```

`waveMask` uses SIDScore wave bits:

```text
bit 0 PULSE
bit 1 SAW
bit 2 TRI
bit 3 NOISE
```

Voice flags:

```text
bit 0 active
bit 1 gate
bit 2 sync
bit 3 ring
bit 4 filter routed
bit 5 done
bit 6 pulse width controlled by table
bit 7 waveform controlled by table
bit 8 pitch controlled by table
bit 9 gate controlled by table
```

`freqReg` is the effective SID frequency register after pitch table effects.
`pulseWidth` is the current 12-bit SID pulse width value.

`envelopeLevel` and `outputLevel` are normalized `0.0..1.0` values intended for
visualization, not sample-accurate synthesis.

## 13. Scope Buckets

Scope buckets provide compact oscilloscope data. They are preferred over raw
sample arrays.

### SCOPE_BUCKETS Payload

```text
u64 scoreId
u64 blockIndex
f32 sampleRate
u16 bucketCount
u16 samplesPerBucket

repeat 3 voices:
  u8 voiceIndex
  u8 reserved
  repeat bucketCount:
    i16 minSample
    i16 maxSample
```

Samples are signed normalized PCM values scaled to `i16`.

The server SHOULD choose bucket sizes based on the SRAP audio block size and the
client capability. A typical first implementation can use 64 buckets per block.

## 14. Error Frames

### ERROR Payload

```text
u32 requestId          0 if not related to a command
u16 code
u16 flags
str message
```

Error codes:

```text
1 unsupported_version
2 invalid_frame
3 invalid_state
4 file_not_found
5 parse_error
6 resolve_error
7 playback_error
8 internal_error
```

Error messages are for diagnostics and logs. Clients should branch on `code`.

## 15. Timing

`frameIndex` means SIDScore player frame, not audio sample. It follows the
resolved score system:

- PAL: 50.124542 frames/second
- NTSC: 60.098814 frames/second

`blockIndex` increments for each emitted telemetry block.

`timestampNanos` in the frame header is sender-local monotonic time and is only
for latency estimation. It is not a wall-clock timestamp.

## 16. Implementation Notes

The Java implementation should introduce a dedicated server entry point, for
example:

```text
net.resheim.sidscore.SIDScorePlayerServer
```

The existing CLI can route `--player-server` to this class or keep it separate.

Recommended Java components:

- `SIDScorePlayerServer`: process mode, TCP server, command dispatch
- `SrapProtocol`: frame constants, encoder, decoder
- `PlaybackTelemetry`: immutable telemetry records
- `ScoreMapExporter`: builds `SCORE_MAP` from parse tree, resolved score, and
  frame event timing
- SRAP listener extension: emits `VOICE_STATE`, `SCOPE_BUCKETS`, and
  `HIGHLIGHT_STATE`

`RealtimeAudioPlayer` already supports `stop()`, `pause()`, and `resume()`. The
server should call these for `STOP`, `PAUSE`, and `CONTINUE`.

The server should avoid letting network I/O block the audio thread. Telemetry
frames should be written through a bounded queue. If the client falls behind,
the server MAY drop `VOICE_STATE` and `SCOPE_BUCKETS` frames, but MUST preserve
`PLAYBACK_STATE`, `SCORE_MAP`, `HIGHLIGHT_STATE`, and `ERROR` ordering.

## 17. Commodore Commander Integration

Commodore Commander backend responsibilities:

- Spawn the Java player server.
- Read the startup JSON line from stdout.
- Connect to the announced loopback port.
- Send `HELLO`.
- Send playback commands from UI actions.
- Decode binary frames.
- Forward editor highlight events and visualization data to the frontend.

Commodore Commander frontend responsibilities:

- Render playback controls.
- Highlight editor ranges using `SCORE_MAP` and `HIGHLIGHT_STATE`.
- Render voice state and scope buckets.

The frontend should not parse SIDScore for playback semantics.

## 18. Compatibility and Versioning

Protocol version 1 is intentionally small. Future versions may add:

- Seek/set-position command
- Raw binary sample streaming
- Multiple concurrent clients
- Remote authenticated mode
- More detailed filter telemetry
- Export of table step names/indices

New optional fields should usually be added through new frame types or flags,
not by changing existing payload layouts within the same protocol version.

