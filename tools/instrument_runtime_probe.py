"""Check instrument changes in the assembled JUnit module fixture."""
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

    for _ in range(2):
        call(init, 1)
        snapshots = []
        for frame in range(101):
            call(play)
            snapshots.append(bytes(cpu.mem[0xD400:0xD419]))
        for frame, wave, ad, sr, pw, routes in [
            (0, 0x40, 0x04, 0xA4, 0x0800, 3),
            (26, 0x10, 0x23, 0x85, 0x0300, 2),
            (51, 0x10, 0x00, 0xF0, 0x0800, 3),
            (76, 0x40, 0x04, 0xA4, 0x0800, 3),
        ]:
            state = snapshots[frame]
            assert state[4] & 0xF1 == wave | 1, (frame, "wave/gate", state.hex())
            assert state[5:7] == bytes([ad, sr]), (frame, "ADSR", state.hex())
            assert int.from_bytes(state[2:4], "little") == pw, (frame, "PW", state.hex())
            assert state[23] & 7 == routes, (frame, "routing", state.hex())
        # D4 + 12 semitones must exceed E4 even with vibrato active.
        assert int.from_bytes(snapshots[26][:2], "little") > int.from_bytes(snapshots[51][:2], "little")
        # Each switch retriggers once, without adding time to the musical timeline.
        assert [i for i, s in enumerate(snapshots) if not s[4] & 1] == [25, 50, 75, 100]
        assert len({s[7:14] for s in snapshots[:100]}) == 1, "Other voice changed"


if __name__ == "__main__":
    run(sys.argv[1])
