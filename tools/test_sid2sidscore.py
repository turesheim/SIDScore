import math
import unittest

import sid2sidscore as sid2


def make_row(frame: int, freq: int, note: str = "C4") -> sid2.DumpRow:
    return sid2.DumpRow(
        frame=frame,
        voices=[
            sid2.VoiceState(freq=freq, note=note, wave=0x41, adsr=(0, 4, 10, 4), pulse=0x0800),
            sid2.VoiceState(),
            sid2.VoiceState(),
        ],
        filter=sid2.FilterState(),
    )


class Sid2SidScoreVibratoTest(unittest.TestCase):
    def test_detects_stable_pitch_vibrato(self) -> None:
        delay = 4
        rate = 16
        amp = 64
        phase = 0
        rows: list[sid2.DumpRow] = []

        for frame in range(96):
            if frame < delay:
                offset = 0.0
            else:
                phase = (phase + rate) & 0xFF
                offset = math.sin((phase / 256.0) * math.pi * 2.0)
                offset *= (amp / 255.0) * sid2.VIBRATO_MAX_SEMITONES
            rows.append(make_row(frame, sid2.freq_reg_from_midi(60 + offset, "PAL")))

        vibrato = sid2.detect_voice_vibrato(rows, 1, len(rows), "PAL")

        self.assertIsNotNone(vibrato)
        assert vibrato is not None
        self.assertEqual(delay, vibrato.delay)
        self.assertEqual(rate, vibrato.rate)
        self.assertGreaterEqual(vibrato.amp, amp - 4)
        self.assertLessEqual(vibrato.amp, amp + 4)
        self.assertEqual(0, vibrato.inc)

    def test_rejects_monotonic_pitch_slide(self) -> None:
        rows = [
            make_row(frame, sid2.freq_reg_from_midi(60 + frame / 95.0, "PAL"))
            for frame in range(96)
        ]

        self.assertIsNone(sid2.detect_voice_vibrato(rows, 1, len(rows), "PAL"))

    def test_instrument_line_emits_vibrato_parameter(self) -> None:
        line = sid2.instrument_line(
            1,
            1,
            sid2.VoiceState(wave=0x40, adsr=(1, 2, 3, 4), pulse=0x0800),
            sid2.VibratoSetting(delay=3, rate=12, amp=40, inc=0),
        )

        self.assertIn("VIBRATO=3,12,40,0", line)


if __name__ == "__main__":
    unittest.main()
