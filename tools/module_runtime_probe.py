"""Execute the module test fixture with the repository's 6502 emulator."""
import sys
from pathlib import Path

from siddump import CPU


class TracedMemory(bytearray):
    def __init__(self):
        super().__init__(0x10000)
        self.writes = []

    def __setitem__(self, address, value):
        if isinstance(address, int) and 0xD400 <= address <= 0xD418:
            self.writes.append((address, value))
        super().__setitem__(address, value)


def run(program):
    payload = Path(program).read_bytes()
    load = int.from_bytes(payload[:2], 'little')
    api = [int.from_bytes(payload[i:i+2], 'little') for i in range(2, 16, 2)]

    def new_cpu():
        cpu = CPU()
        cpu.mem = TracedMemory()
        cpu.mem[load:load+len(payload)-2] = payload[2:]
        call(cpu, 0, 1)
        return cpu

    def call(cpu, entry, a=0):
        cpu.init(api[entry], a, 0, 0)
        cpu.mem[0xFB:0xFD] = b'\x55\xaa'
        for _ in range(100000):
            if not cpu.step():
                break
        else:
            raise AssertionError('Module call failed to return')
        assert cpu.mem[0xFB:0xFD] == b'\x55\xaa', 'Zero-page pointer corrupted'

    def registers(cpu):
        return bytes(cpu.mem[0xD400:0xD419])

    baseline, effect = new_cpu(), new_cpu()
    call(effect, 2)  # Restart
    for tick in range(4):
        call(baseline, 1)
        effect.mem.writes.clear()
        call(effect, 1)
        if tick < 3:
            assert effect.mem[0xD404] == 0x81, 'Music overwrote active effect'
            assert all(value == 0x81 for address, value in effect.mem.writes if address == 0xD404), 'Music gate leaked into effect'
            assert effect.mem[0xD418] == 0x15
            assert all(value == 0x15 for address, value in effect.mem.writes if address == 0xD418), 'Transient music volume leaked into effect'
        else:
            assert registers(effect) == registers(baseline), 'Music/global state not restored after effect'

    # RESTART resets the frame pointer; IGNORE lets it keep advancing.
    effect = new_cpu()
    call(effect, 2)
    call(effect, 1)
    call(effect, 1)
    assert effect.mem[0xD401] == 0x30
    call(effect, 2)
    call(effect, 1)
    assert effect.mem[0xD401] == 0x40, 'RESTART did not restart'
    effect = new_cpu()
    call(effect, 3)
    call(effect, 1)
    call(effect, 3)
    call(effect, 1)
    assert effect.mem[0xD401] == 0x10, 'IGNORE restarted active effect'

    # A higher-priority effect in another tune steals the physical voice;
    # a later low-priority trigger must not overwrite it.
    effect, baseline = new_cpu(), new_cpu()
    call(effect, 2)
    call(effect, 1)
    call(baseline, 1)
    call(effect, 4)
    call(effect, 2)
    for tick in range(3):
        call(effect, 1)
        call(baseline, 1)
        if tick < 2:
            assert effect.mem[0xD401] == 0x60, 'Cross-tune ownership/preemption failed'
        else:
            assert registers(effect) == registers(baseline), 'Stolen effect resumed or music failed to return'

    # ANY runs concurrently on a free voice, then releases it to silence.
    effect = new_cpu()
    call(effect, 2)
    call(effect, 5)
    call(effect, 1)
    assert effect.mem[0xD404] == 0x81
    assert effect.mem[0xD412] == 0x21
    call(effect, 1)
    call(effect, 1)
    assert effect.mem[0xD412] == 0
    assert effect.mem[0xD40E:0xD410] == b'\0\0'

    # Global-only effects do not reserve or suppress a music voice.
    effect, baseline = new_cpu(), new_cpu()
    call(effect, 6)
    for tick in range(3):
        call(effect, 1)
        call(baseline, 1)
        assert effect.mem[0xD400:0xD415] == baseline.mem[0xD400:0xD415]
        if tick < 2:
            assert effect.mem[0xD418] == 0x17
        else:
            assert registers(effect) == registers(baseline)

    # Selecting/restarting a tune clears active effects and global SID state.
    call(effect, 2)
    call(effect, 1)
    call(effect, 0, 1)
    assert registers(effect) == registers(new_cpu())
    for invalid in (0, 4, 255):
        call(effect, 0, invalid)
        assert registers(effect) == registers(new_cpu()), 'Invalid tune did not select tune 1'


if __name__ == '__main__':
    run(sys.argv[1])
