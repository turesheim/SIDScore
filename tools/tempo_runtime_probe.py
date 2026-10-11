"""Verify inline tempo timing in the assembled JUnit module fixture."""
import sys
from pathlib import Path

from siddump import CPU


def run(program):
    payload = Path(program).read_bytes()
    load = int.from_bytes(payload[:2], "little")
    init, play = [int.from_bytes(payload[i:i + 2], "little") for i in (2, 4)]
    cpu = CPU()
    cpu.mem[load:load + len(payload) - 2] = payload[2:]

    def call(address, a=0):
        cpu.init(address, a, 0, 0)
        for _ in range(100000):
            if not cpu.step():
                return
        raise AssertionError("Module call failed to return")

    for tune, first_note_frames, total_frames in [(1, 25, 75), (2, 20, 60), (1, 25, 75)]:
        call(init, tune)
        held_gate = []
        moving_pitch = []
        for _ in range(total_frames + 1):
            call(play)
            held_gate.append(cpu.mem[0xD404] & 1)
            moving_pitch.append(bytes(cpu.mem[0xD407:0xD409]))
        assert held_gate == [1] * total_frames + [0], (tune, "Held note duration/gate", held_gate)
        assert len(set(moving_pitch[:first_note_frames])) == 1, (tune, "Premature note change")
        assert moving_pitch[first_note_frames] != moving_pitch[0], (tune, "Late note change")
        assert len(set(moving_pitch[first_note_frames:total_frames])) == 1, (tune, "Unexpected note change")


if __name__ == "__main__":
    run(sys.argv[1])
