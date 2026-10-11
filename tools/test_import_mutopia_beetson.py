import struct
import tempfile
import unittest
from pathlib import Path

import import_mutopia_beetson as importer


def vlq(value):
    result = [value & 127]
    while value >> 7:
        value >>= 7
        result.insert(0, (value & 127) | 128)
    return bytes(result)


def track(events):
    data = b"".join(vlq(delta) + event for delta, event in events) + b"\x00\xff\x2f\x00"
    return b"MTrk" + struct.pack(">I", len(data)) + data


def tempo(microseconds):
    return b"\xff\x51\x03" + microseconds.to_bytes(3, "big")


class MidiTempoTest(unittest.TestCase):
    def parse(self, tracks):
        data = b"MThd" + struct.pack(">IHHH", 6, 1, len(tracks), 480) + b"".join(tracks)
        return importer.parse_midi(data)

    def test_preserves_conductor_track_changes_and_default_initial_tempo(self):
        song = self.parse([
            track([(480, tempo(1000000)), (480, tempo(1000000)), (480, tempo(400000))]),
            track([(0, b"\x90\x3c\x64"), (1920, b"\x80\x3c\x00")]),
        ])
        self.assertEqual(120, song.tempo_bpm)
        self.assertEqual([(480, 60), (1440, 150)], song.tempo_changes)
        text = importer.sidscore_text(importer.PieceLink("Test", "test.mid", "test"), song)
        self.assertEqual(["C4", "TEMPO", "60", "&", "C2", "TEMPO", "150", "&", "C4"],
                         importer.emit_voice_tokens([(60, 192)], [(48, 60), (144, 150)]))
        self.assertEqual(2, text.count("TEMPO 60") + text.count("TEMPO 150"))

    def test_tempo_is_not_clamped_at_260(self):
        song = self.parse([track([(0, tempo(round(60000000 / 285)))])])
        self.assertEqual(285, song.tempo_bpm)

    def test_invalid_tempo_is_rejected(self):
        for value in [0, 100000]:
            with self.assertRaises(ValueError):
                self.parse([track([(0, tempo(value))])])

    def test_marker_splits_rest_without_tie(self):
        self.assertEqual(["R4", "TEMPO", "60", "R4", "D4"],
                         importer.emit_voice_tokens([(None, 96), (62, 48)], [(48, 60)]))

    def test_marker_on_note_boundary_does_not_tie_next_attack(self):
        self.assertEqual(["C4", "TEMPO", "60", "C4"],
                         importer.emit_voice_tokens([(60, 48), (60, 48)], [(48, 60)]))

    def test_multiple_changes_within_held_note(self):
        self.assertEqual(["C8", "TEMPO", "60", "&", "C8", "TEMPO", "90", "&", "C4"],
                         importer.emit_voice_tokens([(60, 96)], [(24, 60), (48, 90)]))

    def test_repeated_midi_notes_keep_separate_attacks_in_all_voices(self):
        notes = [importer.NoteEvent(start, start + 480, pitch)
                 for start in [0, 480] for pitch in [48, 60, 72]]
        song = importer.MidiSong(480, 120, 4, 4, notes)
        segments = importer.extract_voice_segments(song)
        self.assertEqual([[(72, 48), (72, 48)], [(60, 48), (60, 48)], [(48, 48), (48, 48)]], segments)

    def test_held_note_does_not_retrigger_when_another_pitch_starts(self):
        song = importer.MidiSong(480, 120, 4, 4, [
            importer.NoteEvent(0, 960, 72), importer.NoteEvent(0, 480, 48),
            importer.NoteEvent(480, 960, 50),
        ])
        self.assertEqual([(72, 96)], importer.extract_voice_segments(song)[0])

    def test_generated_score_embeds_instruments(self):
        song = importer.MidiSong(480, 120, 4, 4, [importer.NoteEvent(0, 480, 60)])
        text = importer.sidscore_text(importer.PieceLink("Test", "test.mid", "test"), song)
        self.assertIn("INSTR piano_hi\n  WAVE=PULSE", text)
        self.assertIn("INSTR piano_mid\n  WAVE=TRI", text)
        self.assertIn("INSTR piano_low\n  WAVE=TRI", text)
        self.assertNotIn(".sidscore\"", text)

    def test_local_regeneration_retains_metadata_and_is_repeatable(self):
        data = b"MThd" + struct.pack(">IHHH", 6, 0, 1, 480) + track([
            (0, b"\x90\x3c\x64"), (480, b"\x80\x3c\x00"),
        ])
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "piece.mid").write_bytes(data)
            score = root / "piece.sidscore"
            score.write_text('TITLE "Sonata \\ Op. 111"\n; Source: https://example.org/piece.mid\n')
            self.assertEqual(1, importer.regenerate_local_scores(root))
            first = score.read_text()
            self.assertIn('TITLE "Sonata \\ Op. 111"', first)
            self.assertIn('; Source: https://example.org/piece.mid', first)
            importer.regenerate_local_scores(root)
            self.assertEqual(first, score.read_text())


if __name__ == "__main__":
    unittest.main()
