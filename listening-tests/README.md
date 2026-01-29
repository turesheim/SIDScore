# Listening tests

This folder contains audio artifacts for manual listening and regression checks.

What you are hearing:

- The SID files are built from the ASM driver and played by SID players (e.g., VSID/reSID). They represent hardware playback.
- The WAV files are rendered by RTAP (RealtimeAudioPlayer) with a specific waveform table model.
- If RTAP is launched from a directory that does not include `waveforms/`, it falls back to the internal clean-room waveform generator and will sound different from hardware (often brighter/cleaner).

Naming scheme:

- `*-6581.*` or `*-8580.*` indicates the SID model used.
- `*-rtap-6581.wav` / `*-rtap-8580.wav` indicates RTAP’s internal clean-room waveform generator (no external tables).
- WAVs without `-rtap-` are rendered with `--sid-waveforms waveforms` (MAME-derived combined waveforms, hardware-like).

Files (examples):

- `menuet_k2-simple-6581.sid`, `menuet_k2-6581.sid`: hardware-targeted SID files (model flagged in PSID header).
- `menuet_k2-simple-6581.wav`, `menuet_k2-6581.wav`: RTAP render using MAME waveform tables (hardware-like).
- `menuet_k2-simple-rtap-6581.wav`, `menuet_k2-rtap-6581.wav`: RTAP render using clean-room waveform tables.
- 8580 variants follow the same naming pattern.

Regenerate:

- Run `./generate.sh` from this folder to rebuild all 6581/8580 SID/WAV artifacts.
- The script builds the Java module and uses the same CLI options as the manual commands.

If you want RTAP to match hardware in future runs, pass:
- `--sid-model <6581|8580> --sid-waveforms waveforms`
