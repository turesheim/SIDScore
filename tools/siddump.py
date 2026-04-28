#!/usr/bin/env python3
#
# Python port of SIDDump V1.10:
#   https://github.com/cadaver/siddump
#
# Original copyright notice retained for this derived implementation:
#
# SIDDump V1.10
# by Lasse Oorni (loorni@gmail.com) and Stein Pedersen
#
# Copyright (C) 2005-2026 by the authors. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. The name of the author may not be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
# EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
# OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
# ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from __future__ import annotations

from dataclasses import dataclass, replace
import math
from pathlib import Path
import sys


MAX_INSTR = 0x100000

FN = 0x80
FV = 0x40
FB = 0x10
FD = 0x08
FI = 0x04
FZ = 0x02
FC = 0x01


NOTENAMES = [
    "C-0", "C#0", "D-0", "D#0", "E-0", "F-0", "F#0", "G-0", "G#0", "A-0", "A#0", "B-0",
    "C-1", "C#1", "D-1", "D#1", "E-1", "F-1", "F#1", "G-1", "G#1", "A-1", "A#1", "B-1",
    "C-2", "C#2", "D-2", "D#2", "E-2", "F-2", "F#2", "G-2", "G#2", "A-2", "A#2", "B-2",
    "C-3", "C#3", "D-3", "D#3", "E-3", "F-3", "F#3", "G-3", "G#3", "A-3", "A#3", "B-3",
    "C-4", "C#4", "D-4", "D#4", "E-4", "F-4", "F#4", "G-4", "G#4", "A-4", "A#4", "B-4",
    "C-5", "C#5", "D-5", "D#5", "E-5", "F-5", "F#5", "G-5", "G#5", "A-5", "A#5", "B-5",
    "C-6", "C#6", "D-6", "D#6", "E-6", "F-6", "F#6", "G-6", "G#6", "A-6", "A#6", "B-6",
    "C-7", "C#7", "D-7", "D#7", "E-7", "F-7", "F#7", "G-7", "G#7", "A-7", "A#7", "B-7",
]

FILTERNAMES = ["Off", "Low", "Bnd", "L+B", "Hi ", "L+H", "B+H", "LBH"]

DEFAULT_FREQ_LO = [
    0x17, 0x27, 0x39, 0x4B, 0x5F, 0x74, 0x8A, 0xA1, 0xBA, 0xD4, 0xF0, 0x0E,
    0x2D, 0x4E, 0x71, 0x96, 0xBE, 0xE8, 0x14, 0x43, 0x74, 0xA9, 0xE1, 0x1C,
    0x5A, 0x9C, 0xE2, 0x2D, 0x7C, 0xCF, 0x28, 0x85, 0xE8, 0x52, 0xC1, 0x37,
    0xB4, 0x39, 0xC5, 0x5A, 0xF7, 0x9E, 0x4F, 0x0A, 0xD1, 0xA3, 0x82, 0x6E,
    0x68, 0x71, 0x8A, 0xB3, 0xEE, 0x3C, 0x9E, 0x15, 0xA2, 0x46, 0x04, 0xDC,
    0xD0, 0xE2, 0x14, 0x67, 0xDD, 0x79, 0x3C, 0x29, 0x44, 0x8D, 0x08, 0xB8,
    0xA1, 0xC5, 0x28, 0xCD, 0xBA, 0xF1, 0x78, 0x53, 0x87, 0x1A, 0x10, 0x71,
    0x42, 0x89, 0x4F, 0x9B, 0x74, 0xE2, 0xF0, 0xA6, 0x0E, 0x33, 0x20, 0xFF,
]

DEFAULT_FREQ_HI = [
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x02,
    0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x03, 0x03, 0x03, 0x03, 0x03, 0x04,
    0x04, 0x04, 0x04, 0x05, 0x05, 0x05, 0x06, 0x06, 0x06, 0x07, 0x07, 0x08,
    0x08, 0x09, 0x09, 0x0A, 0x0A, 0x0B, 0x0C, 0x0D, 0x0D, 0x0E, 0x0F, 0x10,
    0x11, 0x12, 0x13, 0x14, 0x15, 0x17, 0x18, 0x1A, 0x1B, 0x1D, 0x1F, 0x20,
    0x22, 0x24, 0x27, 0x29, 0x2B, 0x2E, 0x31, 0x34, 0x37, 0x3A, 0x3E, 0x41,
    0x45, 0x49, 0x4E, 0x52, 0x57, 0x5C, 0x62, 0x68, 0x6E, 0x75, 0x7C, 0x83,
    0x8B, 0x93, 0x9C, 0xA5, 0xAF, 0xB9, 0xC4, 0xD0, 0xDD, 0xEA, 0xF8, 0xFF,
]

CPU_CYCLES = [
    7, 6, 0, 8, 3, 3, 5, 5, 3, 2, 2, 2, 4, 4, 6, 6,
    2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    6, 6, 0, 8, 3, 3, 5, 5, 4, 2, 2, 2, 4, 4, 6, 6,
    2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    6, 6, 0, 8, 3, 3, 5, 5, 3, 2, 2, 2, 3, 4, 6, 6,
    2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    6, 6, 0, 8, 3, 3, 5, 5, 4, 2, 2, 2, 5, 4, 6, 6,
    2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
    2, 6, 0, 6, 4, 4, 4, 4, 2, 5, 2, 5, 5, 5, 5, 5,
    2, 6, 2, 6, 3, 3, 3, 3, 2, 2, 2, 2, 4, 4, 4, 4,
    2, 5, 0, 5, 4, 4, 4, 4, 2, 4, 2, 4, 4, 4, 4, 4,
    2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
    2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
    2, 6, 2, 8, 3, 3, 5, 5, 2, 2, 2, 2, 4, 4, 6, 6,
    2, 5, 0, 8, 4, 4, 6, 6, 2, 4, 2, 7, 4, 4, 7, 7,
]

ADC_OPS = {0x69: "imm", 0x65: "zp", 0x75: "zpx", 0x6D: "abs", 0x7D: "absx", 0x79: "absy", 0x61: "indx", 0x71: "indy"}
AND_OPS = {0x29: "imm", 0x25: "zp", 0x35: "zpx", 0x2D: "abs", 0x3D: "absx", 0x39: "absy", 0x21: "indx", 0x31: "indy"}
CMP_OPS = {0xC9: "imm", 0xC5: "zp", 0xD5: "zpx", 0xCD: "abs", 0xDD: "absx", 0xD9: "absy", 0xC1: "indx", 0xD1: "indy"}
EOR_OPS = {0x49: "imm", 0x45: "zp", 0x55: "zpx", 0x4D: "abs", 0x5D: "absx", 0x59: "absy", 0x41: "indx", 0x51: "indy"}
LDA_OPS = {0xA9: "imm", 0xA5: "zp", 0xB5: "zpx", 0xAD: "abs", 0xBD: "absx", 0xB9: "absy", 0xA1: "indx", 0xB1: "indy"}
ORA_OPS = {0x09: "imm", 0x05: "zp", 0x15: "zpx", 0x0D: "abs", 0x1D: "absx", 0x19: "absy", 0x01: "indx", 0x11: "indy"}
SBC_OPS = {0xE9: "imm", 0xEB: "imm", 0xE5: "zp", 0xF5: "zpx", 0xED: "abs", 0xFD: "absx", 0xF9: "absy", 0xE1: "indx", 0xF1: "indy"}
READ_PAGE_MODES = {"absx", "absy", "indy"}


class CPUError(RuntimeError):
    pass


class CPU:
    def __init__(self) -> None:
        self.mem = bytearray(0x10000)
        self.pc = 0
        self.a = 0
        self.x = 0
        self.y = 0
        self.flags = 0
        self.sp = 0xFF
        self.cpucycles = 0

    def init(self, newpc: int, newa: int, newx: int, newy: int) -> None:
        self.pc = newpc & 0xFFFF
        self.a = newa & 0xFF
        self.x = newx & 0xFF
        self.y = newy & 0xFF
        self.flags = 0
        self.sp = 0xFF
        self.cpucycles = 0

    def _fetch(self) -> int:
        value = self.mem[self.pc]
        self.pc = (self.pc + 1) & 0xFFFF
        return value

    def _lo(self) -> int:
        return self.mem[self.pc]

    def _hi(self) -> int:
        return self.mem[(self.pc + 1) & 0xFFFF]

    def _absolute(self) -> int:
        return self._lo() | (self._hi() << 8)

    def _indirect_zp(self) -> int:
        zp = self._lo()
        return self.mem[zp] | (self.mem[(zp + 1) & 0xFF] << 8)

    def _page_crossed(self, base: int, real: int) -> int:
        return 1 if ((base ^ real) & 0xFF00) else 0

    def _addr(self, mode: str) -> tuple[int, int, int | None]:
        if mode == "imm":
            return self.pc, 1, None
        if mode == "zp":
            return self._lo() & 0xFF, 1, None
        if mode == "zpx":
            return (self._lo() + self.x) & 0xFF, 1, None
        if mode == "zpy":
            return (self._lo() + self.y) & 0xFF, 1, None
        if mode == "abs":
            return self._absolute(), 2, None
        if mode == "absx":
            base = self._absolute()
            return (base + self.x) & 0xFFFF, 2, base
        if mode == "absy":
            base = self._absolute()
            return (base + self.y) & 0xFFFF, 2, base
        if mode == "indx":
            zp = (self._lo() + self.x) & 0xFF
            return self.mem[zp] | (self.mem[(zp + 1) & 0xFF] << 8), 1, None
        if mode == "indy":
            base = self._indirect_zp()
            return (base + self.y) & 0xFFFF, 1, base
        raise CPUError(f"Internal error: unknown addressing mode {mode}")

    def _read(self, mode: str, page_cycle: bool = False) -> int:
        addr, size, base = self._addr(mode)
        if page_cycle and base is not None:
            self.cpucycles += self._page_crossed(base, addr)
        value = self.mem[addr]
        self.pc = (self.pc + size) & 0xFFFF
        return value

    def _write(self, mode: str, value: int) -> None:
        addr, size, _base = self._addr(mode)
        self.mem[addr] = value & 0xFF
        self.pc = (self.pc + size) & 0xFFFF

    def _rmw(self, mode: str, operation) -> None:
        addr, size, _base = self._addr(mode)
        self.mem[addr] = operation(self.mem[addr]) & 0xFF
        self.pc = (self.pc + size) & 0xFFFF

    def _push(self, value: int) -> None:
        self.mem[0x100 + self.sp] = value & 0xFF
        self.sp = (self.sp - 1) & 0xFF

    def _pop(self) -> int:
        self.sp = (self.sp + 1) & 0xFF
        return self.mem[0x100 + self.sp]

    def _set_nz(self, value: int) -> None:
        value &= 0xFF
        if value == 0:
            self.flags = (self.flags & ~FN) | FZ
        else:
            self.flags = (self.flags & ~(FN | FZ)) | (value & FN)

    def _assign_a(self, value: int) -> None:
        self.a = value & 0xFF
        self._set_nz(self.a)

    def _assign_x(self, value: int) -> None:
        self.x = value & 0xFF
        self._set_nz(self.x)

    def _assign_y(self, value: int) -> None:
        self.y = value & 0xFF
        self._set_nz(self.y)

    def _adc(self, value: int) -> None:
        tempval = value & 0xFF
        old_a = self.a
        carry = self.flags & FC
        if self.flags & FD:
            temp = (old_a & 0x0F) + (tempval & 0x0F) + carry
            if temp > 0x09:
                temp += 0x06
            if temp <= 0x0F:
                temp = (temp & 0x0F) + (old_a & 0xF0) + (tempval & 0xF0)
            else:
                temp = (temp & 0x0F) + (old_a & 0xF0) + (tempval & 0xF0) + 0x10

            if not ((old_a + tempval + carry) & 0xFF):
                self.flags |= FZ
            else:
                self.flags &= ~FZ
            if temp & 0x80:
                self.flags |= FN
            else:
                self.flags &= ~FN
            if ((old_a ^ temp) & 0x80) and not ((old_a ^ tempval) & 0x80):
                self.flags |= FV
            else:
                self.flags &= ~FV
            if (temp & 0x1F0) > 0x90:
                temp += 0x60
            if (temp & 0xFF0) > 0xF0:
                self.flags |= FC
            else:
                self.flags &= ~FC
        else:
            temp = tempval + old_a + carry
            self._set_nz(temp)
            if not ((old_a ^ tempval) & 0x80) and ((old_a ^ temp) & 0x80):
                self.flags |= FV
            else:
                self.flags &= ~FV
            if temp > 0xFF:
                self.flags |= FC
            else:
                self.flags &= ~FC
        self.a = temp & 0xFF

    def _sbc(self, value: int) -> None:
        tempval = value & 0xFF
        old_a = self.a
        borrow = (self.flags & FC) ^ FC
        temp = (old_a - tempval - borrow) & 0xFFFFFFFF
        if self.flags & FD:
            tempval2 = ((old_a & 0x0F) - (tempval & 0x0F) - borrow) & 0xFFFFFFFF
            if tempval2 & 0x10:
                high = ((old_a & 0xF0) - (tempval & 0xF0) - 0x10) & 0xFFFFFFFF
                tempval2 = (((tempval2 - 6) & 0x0F) | high) & 0xFFFFFFFF
            else:
                high = ((old_a & 0xF0) - (tempval & 0xF0)) & 0xFFFFFFFF
                tempval2 = ((tempval2 & 0x0F) | high) & 0xFFFFFFFF
            if tempval2 & 0x100:
                tempval2 = (tempval2 - 0x60) & 0xFFFFFFFF
            if temp < 0x100:
                self.flags |= FC
            else:
                self.flags &= ~FC
            self._set_nz(temp)
            if ((old_a ^ temp) & 0x80) and ((old_a ^ tempval) & 0x80):
                self.flags |= FV
            else:
                self.flags &= ~FV
            self.a = tempval2 & 0xFF
        else:
            self._set_nz(temp)
            if temp < 0x100:
                self.flags |= FC
            else:
                self.flags &= ~FC
            if ((old_a ^ temp) & 0x80) and ((old_a ^ tempval) & 0x80):
                self.flags |= FV
            else:
                self.flags &= ~FV
            self.a = temp & 0xFF

    def _cmp(self, source: int, value: int) -> None:
        value &= 0xFF
        source &= 0xFF
        temp = (source - value) & 0xFF
        self.flags = (self.flags & ~(FC | FN | FZ)) | (temp & FN)
        if temp == 0:
            self.flags |= FZ
        if source >= value:
            self.flags |= FC

    def _asl(self, value: int) -> int:
        temp = (value & 0xFF) << 1
        if temp & 0x100:
            self.flags |= FC
        else:
            self.flags &= ~FC
        result = temp & 0xFF
        self._set_nz(result)
        return result

    def _lsr(self, value: int) -> int:
        temp = value & 0xFF
        if temp & 1:
            self.flags |= FC
        else:
            self.flags &= ~FC
        result = (temp >> 1) & 0xFF
        self._set_nz(result)
        return result

    def _rol(self, value: int) -> int:
        temp = (value & 0xFF) << 1
        if self.flags & FC:
            temp |= 1
        if temp & 0x100:
            self.flags |= FC
        else:
            self.flags &= ~FC
        result = temp & 0xFF
        self._set_nz(result)
        return result

    def _ror(self, value: int) -> int:
        temp = value & 0xFF
        if self.flags & FC:
            temp |= 0x100
        if temp & 1:
            self.flags |= FC
        else:
            self.flags &= ~FC
        result = (temp >> 1) & 0xFF
        self._set_nz(result)
        return result

    def _dec(self, value: int) -> int:
        result = (value - 1) & 0xFF
        self._set_nz(result)
        return result

    def _inc(self, value: int) -> int:
        result = (value + 1) & 0xFF
        self._set_nz(result)
        return result

    def _and(self, value: int) -> None:
        self.a = (self.a & value) & 0xFF
        self._set_nz(self.a)

    def _eor(self, value: int) -> None:
        self.a = (self.a ^ value) & 0xFF
        self._set_nz(self.a)

    def _ora(self, value: int) -> None:
        self.a = (self.a | value) & 0xFF
        self._set_nz(self.a)

    def _bit(self, value: int) -> None:
        value &= 0xFF
        self.flags = (self.flags & ~(FN | FV)) | (value & (FN | FV))
        if not (value & self.a):
            self.flags |= FZ
        else:
            self.flags &= ~FZ

    def _branch(self) -> None:
        self.cpucycles += 1
        temp = self._fetch()
        if temp < 0x80:
            target = (self.pc + temp) & 0xFFFF
        else:
            target = (self.pc + temp - 0x100) & 0xFFFF
        self.cpucycles += self._page_crossed(self.pc, target)
        self.pc = target

    def step(self) -> bool:
        op_pc = self.pc
        op = self._fetch()
        self.cpucycles += CPU_CYCLES[op]

        if op in ADC_OPS:
            mode = ADC_OPS[op]
            self._adc(self._read(mode, mode in READ_PAGE_MODES))
        elif op in AND_OPS:
            mode = AND_OPS[op]
            self._and(self._read(mode, mode in READ_PAGE_MODES))
        elif op in CMP_OPS:
            mode = CMP_OPS[op]
            self._cmp(self.a, self._read(mode, mode in READ_PAGE_MODES))
        elif op in EOR_OPS:
            mode = EOR_OPS[op]
            self._eor(self._read(mode, mode in READ_PAGE_MODES))
        elif op in LDA_OPS:
            mode = LDA_OPS[op]
            self._assign_a(self._read(mode, mode in READ_PAGE_MODES))
        elif op in ORA_OPS:
            mode = ORA_OPS[op]
            self._ora(self._read(mode, mode in READ_PAGE_MODES))
        elif op in SBC_OPS:
            mode = SBC_OPS[op]
            self._sbc(self._read(mode, mode in READ_PAGE_MODES))

        elif op == 0xA7:
            self._assign_a(self._read("zp"))
            self.x = self.a
        elif op == 0xB7:
            self._assign_a(self._read("zpy"))
            self.x = self.a
        elif op == 0xAF:
            self._assign_a(self._read("abs"))
            self.x = self.a
        elif op == 0xA3:
            self._assign_a(self._read("indx"))
            self.x = self.a
        elif op == 0xB3:
            self._assign_a(self._read("indy", True))
            self.x = self.a

        elif op in {0x1A, 0x3A, 0x5A, 0x7A, 0xDA, 0xEA, 0xFA}:
            pass
        elif op in {0x80, 0x82, 0x89, 0xC2, 0xE2, 0x04, 0x44, 0x64, 0x14, 0x34, 0x54, 0x74, 0xD4, 0xF4}:
            self.pc = (self.pc + 1) & 0xFFFF
        elif op in {0x0C, 0x1C, 0x3C, 0x5C, 0x7C, 0xDC, 0xFC}:
            base = self._absolute()
            addr = (base + self.x) & 0xFFFF
            self.cpucycles += self._page_crossed(base, addr)
            self.pc = (self.pc + 2) & 0xFFFF
        elif op in {0x0B, 0x2B}:
            value = self._read("imm")
            self._and(value)
            if value & 0x80:
                self.flags |= FC
            else:
                self.flags &= ~FC

        elif op == 0x0A:
            self.a = self._asl(self.a)
        elif op == 0x06:
            self._rmw("zp", self._asl)
        elif op == 0x16:
            self._rmw("zpx", self._asl)
        elif op == 0x0E:
            self._rmw("abs", self._asl)
        elif op == 0x1E:
            self._rmw("absx", self._asl)

        elif op == 0x90:
            self._branch() if not (self.flags & FC) else self._skip_operand()
        elif op == 0xB0:
            self._branch() if (self.flags & FC) else self._skip_operand()
        elif op == 0xF0:
            self._branch() if (self.flags & FZ) else self._skip_operand()
        elif op == 0x30:
            self._branch() if (self.flags & FN) else self._skip_operand()
        elif op == 0xD0:
            self._branch() if not (self.flags & FZ) else self._skip_operand()
        elif op == 0x10:
            self._branch() if not (self.flags & FN) else self._skip_operand()
        elif op == 0x50:
            self._branch() if not (self.flags & FV) else self._skip_operand()
        elif op == 0x70:
            self._branch() if (self.flags & FV) else self._skip_operand()

        elif op == 0x24:
            self._bit(self._read("zp"))
        elif op == 0x2C:
            self._bit(self._read("abs"))
        elif op == 0x18:
            self.flags &= ~FC
        elif op == 0xD8:
            self.flags &= ~FD
        elif op == 0x58:
            self.flags &= ~FI
        elif op == 0xB8:
            self.flags &= ~FV

        elif op == 0xE0:
            self._cmp(self.x, self._read("imm"))
        elif op == 0xE4:
            self._cmp(self.x, self._read("zp"))
        elif op == 0xEC:
            self._cmp(self.x, self._read("abs"))
        elif op == 0xC0:
            self._cmp(self.y, self._read("imm"))
        elif op == 0xC4:
            self._cmp(self.y, self._read("zp"))
        elif op == 0xCC:
            self._cmp(self.y, self._read("abs"))

        elif op == 0xC6:
            self._rmw("zp", self._dec)
        elif op == 0xD6:
            self._rmw("zpx", self._dec)
        elif op == 0xCE:
            self._rmw("abs", self._dec)
        elif op == 0xDE:
            self._rmw("absx", self._dec)
        elif op == 0xCA:
            self.x = (self.x - 1) & 0xFF
            self._set_nz(self.x)
        elif op == 0x88:
            self.y = (self.y - 1) & 0xFF
            self._set_nz(self.y)

        elif op == 0xE6:
            self._rmw("zp", self._inc)
        elif op == 0xF6:
            self._rmw("zpx", self._inc)
        elif op == 0xEE:
            self._rmw("abs", self._inc)
        elif op == 0xFE:
            self._rmw("absx", self._inc)
        elif op == 0xE8:
            self.x = (self.x + 1) & 0xFF
            self._set_nz(self.x)
        elif op == 0xC8:
            self.y = (self.y + 1) & 0xFF
            self._set_nz(self.y)

        elif op == 0x20:
            self._push((self.pc + 1) >> 8)
            self._push((self.pc + 1) & 0xFF)
            self.pc = self._absolute()
        elif op == 0x4C:
            self.pc = self._absolute()
        elif op == 0x6C:
            addr = self._absolute()
            self.pc = self.mem[addr] | (self.mem[((addr + 1) & 0xFF) | (addr & 0xFF00)] << 8)

        elif op == 0xA2:
            self._assign_x(self._read("imm"))
        elif op == 0xA6:
            self._assign_x(self._read("zp"))
        elif op == 0xB6:
            self._assign_x(self._read("zpy"))
        elif op == 0xAE:
            self._assign_x(self._read("abs"))
        elif op == 0xBE:
            self._assign_x(self._read("absy", True))
        elif op == 0xA0:
            self._assign_y(self._read("imm"))
        elif op == 0xA4:
            self._assign_y(self._read("zp"))
        elif op == 0xB4:
            self._assign_y(self._read("zpx"))
        elif op == 0xAC:
            self._assign_y(self._read("abs"))
        elif op == 0xBC:
            self._assign_y(self._read("absx", True))

        elif op == 0x4A:
            self.a = self._lsr(self.a)
        elif op == 0x46:
            self._rmw("zp", self._lsr)
        elif op == 0x56:
            self._rmw("zpx", self._lsr)
        elif op == 0x4E:
            self._rmw("abs", self._lsr)
        elif op == 0x5E:
            self._rmw("absx", self._lsr)

        elif op == 0x48:
            self._push(self.a)
        elif op == 0x08:
            self._push(self.flags | 0x30)
        elif op == 0x68:
            self._assign_a(self._pop())
        elif op == 0x28:
            self.flags = self._pop()

        elif op == 0x2A:
            self.a = self._rol(self.a)
        elif op == 0x26:
            self._rmw("zp", self._rol)
        elif op == 0x36:
            self._rmw("zpx", self._rol)
        elif op == 0x2E:
            self._rmw("abs", self._rol)
        elif op == 0x3E:
            self._rmw("absx", self._rol)
        elif op == 0x6A:
            self.a = self._ror(self.a)
        elif op == 0x66:
            self._rmw("zp", self._ror)
        elif op == 0x76:
            self._rmw("zpx", self._ror)
        elif op == 0x6E:
            self._rmw("abs", self._ror)
        elif op == 0x7E:
            self._rmw("absx", self._ror)

        elif op == 0x40:
            if self.sp == 0xFF:
                return False
            self.flags = self._pop()
            self.pc = self._pop()
            self.pc |= self._pop() << 8
        elif op == 0x60:
            if self.sp == 0xFF:
                return False
            self.pc = self._pop()
            self.pc |= self._pop() << 8
            self.pc = (self.pc + 1) & 0xFFFF

        elif op == 0x38:
            self.flags |= FC
        elif op == 0xF8:
            self.flags |= FD
        elif op == 0x78:
            self.flags |= FI

        elif op == 0x85:
            self._write("zp", self.a)
        elif op == 0x95:
            self._write("zpx", self.a)
        elif op == 0x8D:
            self._write("abs", self.a)
        elif op == 0x9D:
            self._write("absx", self.a)
        elif op == 0x99:
            self._write("absy", self.a)
        elif op == 0x81:
            self._write("indx", self.a)
        elif op == 0x91:
            self._write("indy", self.a)
        elif op == 0x86:
            self._write("zp", self.x)
        elif op == 0x96:
            self._write("zpy", self.x)
        elif op == 0x8E:
            self._write("abs", self.x)
        elif op == 0x84:
            self._write("zp", self.y)
        elif op == 0x94:
            self._write("zpx", self.y)
        elif op == 0x8C:
            self._write("abs", self.y)

        elif op == 0xAA:
            self._assign_x(self.a)
        elif op == 0xBA:
            self._assign_x(self.sp)
        elif op == 0x8A:
            self._assign_a(self.x)
        elif op == 0x9A:
            self.sp = self.x
        elif op == 0x98:
            self._assign_a(self.y)
        elif op == 0xA8:
            self._assign_y(self.a)
        elif op == 0x00:
            return False
        elif op == 0x02:
            raise CPUError(f"Error: CPU halt at {op_pc:04X}")
        else:
            raise CPUError(f"Error: Unknown opcode ${op:02X} at ${op_pc:04X}")

        return True

    def _skip_operand(self) -> None:
        self.pc = (self.pc + 1) & 0xFFFF


@dataclass
class Channel:
    freq: int = 0
    pulse: int = 0
    adsr: int = 0
    wave: int = 0
    note: int = 0


@dataclass
class Filter:
    cutoff: int = 0
    ctrl: int = 0
    type: int = 0


@dataclass
class Options:
    sidname: str | None = None
    subtune: int = 0
    seconds: int = 60
    spacing: list[int] | None = None
    pattspacing: int = 0
    firstframe: int = 0
    basefreq: int = 0
    basenote: int = 0xB0
    lowres: bool = False
    oldnotefactor: int = 1
    timeseconds: bool = False
    profiling: bool = False
    detect8580: bool = False
    usage: bool = False

    def __post_init__(self) -> None:
        if self.spacing is None:
            self.spacing = [0, 0]


def usage() -> str:
    return (
        "Usage: SIDDUMP <sidfile> [options]\n"
        "Warning: CPU emulation may be buggy/inaccurate, illegals support very limited\n\n"
        "Options:\n"
        "-a<value> Accumulator value on init (subtune number) default = 0\n"
        "-c<value> Frequency recalibration. Give note frequency in hex\n"
        "-d<value> Select calibration note (abs.notation 80-DF). Default middle-C (B0)\n"
        "-f<value> First frame to display, default 0\n"
        "-l        Low-resolution mode (only display 1 row per note)\n"
        "-n<value> Note spacing, default 0 (none). Use <value>,<value> to specify a funktempo\n"
        "-o<value> Oldnote-sticky factor. Default 1, increase for better vibrato display\n"
        "          (when increased, requires well calibrated frequencies)\n"
        "-p<value> Pattern spacing, default 0 (none)\n"
        "-s        Display time in minutes:seconds:frame format\n"
        "-t<value> Playback time in seconds, default 60\n"
        "-z        Include CPU cycles+rastertime (PAL)+rastertime, badline corrected\n"
        "-8        8580 detection hack\n"
    )


def _int_or_zero(value: str, base: int = 10) -> int:
    if not value:
        return 0
    return int(value, base)


def parse_args(argv: list[str]) -> Options:
    opts = Options()
    for arg in argv[1:]:
        if arg in {"--help", "-h"}:
            opts.usage = True
            continue
        if arg.startswith("-") and len(arg) >= 2:
            option = arg[1].upper()
            value = arg[2:]
            if option == "?":
                opts.usage = True
            elif option == "A":
                opts.subtune = _int_or_zero(value, 10)
            elif option == "C":
                opts.basefreq = _int_or_zero(value, 16)
            elif option == "D":
                opts.basenote = _int_or_zero(value, 16)
            elif option == "F":
                opts.firstframe = _int_or_zero(value, 10)
            elif option == "L":
                opts.lowres = True
            elif option == "N":
                if "," in value:
                    first, second = value.split(",", 1)
                    opts.spacing = [_int_or_zero(first, 10), _int_or_zero(second, 10)]
                else:
                    spacing = _int_or_zero(value, 10)
                    opts.spacing = [spacing, spacing]
            elif option == "O":
                opts.oldnotefactor = max(1, _int_or_zero(value, 10))
            elif option == "P":
                opts.pattspacing = _int_or_zero(value, 10)
            elif option == "S":
                opts.timeseconds = True
            elif option == "T":
                opts.seconds = _int_or_zero(value, 10)
            elif option == "Z":
                opts.profiling = True
            elif option == "8":
                opts.detect8580 = True
        elif opts.sidname is None:
            opts.sidname = arg
    return opts


def be16(data: bytes, offset: int) -> int:
    if offset + 2 > len(data):
        raise ValueError("SID header is truncated.")
    return (data[offset] << 8) | data[offset + 1]


def load_sid(path: Path, cpu: CPU) -> tuple[int, int, int]:
    data = path.read_bytes()
    if len(data) < 0x76:
        raise ValueError("SID file is too short.")

    dataoffset = be16(data, 6)
    loadaddress = be16(data, 8)
    initaddress = be16(data, 10)
    playaddress = be16(data, 12)
    if dataoffset > len(data):
        raise ValueError("SID data offset is past end of file.")

    loadpos = dataoffset
    if loadaddress == 0:
        if loadpos + 2 > len(data):
            raise ValueError("SID payload is missing its load address.")
        loadaddress = data[loadpos] | (data[loadpos + 1] << 8)
        loadpos += 2

    payload = data[loadpos:]
    if len(payload) + loadaddress >= 0x10000:
        raise ValueError("Error: SID data continues past end of C64 memory.")

    cpu.mem[loadaddress:loadaddress + len(payload)] = payload
    return loadaddress, initaddress, playaddress


def recalibrate_freqs(basefreq: int, basenote: int) -> tuple[list[int], list[int], str | None]:
    freqtbllo = DEFAULT_FREQ_LO.copy()
    freqtblhi = DEFAULT_FREQ_HI.copy()
    if not basefreq:
        return freqtbllo, freqtblhi, None

    basenote &= 0x7F
    if basenote < 0 or basenote > 96:
        return freqtbllo, freqtblhi, "Warning: Calibration note out of range. Aborting recalibration."

    for note_index in range(96):
        note = note_index - basenote
        freq = float(basefreq) * math.pow(2.0, note / 12.0)
        if freq > 0xFFFF:
            freq = 0xFFFF
        f = int(freq)
        freqtbllo[note_index] = f & 0xFF
        freqtblhi[note_index] = (f >> 8) & 0xFF
    return freqtbllo, freqtblhi, None


def print_header(opts: Options, loadaddress: int, initaddress: int, playaddress: int, middle_c: int) -> None:
    print(f"Load address: ${loadaddress:04X} Init address: ${initaddress:04X} Play address: ${playaddress:04X}")
    print(f"Calling initroutine with subtune {opts.subtune}")


def dump_sid(opts: Options) -> int:
    if opts.lowres and not opts.spacing[0]:
        opts.lowres = False

    if not opts.sidname:
        print("Error: no SID file specified.")
        return 1

    sidpath = Path(opts.sidname)
    if not sidpath.exists():
        print("Error: couldn't open SID file.")
        return 1

    freqtbllo, freqtblhi, warning = recalibrate_freqs(opts.basefreq, opts.basenote)
    if warning:
        print(warning)

    cpu = CPU()
    try:
        loadaddress, initaddress, playaddress = load_sid(sidpath, cpu)
    except OSError:
        print("Error: couldn't open SID file.")
        return 1
    except ValueError as exc:
        print(exc)
        return 1

    print_header(opts, loadaddress, initaddress, playaddress, freqtbllo[48] | (freqtblhi[48] << 8))
    cpu.mem[0x01] = 0x37
    cpu.init(initaddress, opts.subtune, 0, 0)
    instr = 0
    while cpu.step():
        cpu.mem[0xD41B] = 0x02 if opts.detect8580 else 0x03
        cpu.mem[0xD012] = (cpu.mem[0xD012] + 1) & 0xFF
        if (not cpu.mem[0xD012]) or ((cpu.mem[0xD011] & 0x80) and cpu.mem[0xD012] >= 0x38):
            cpu.mem[0xD011] ^= 0x80
            cpu.mem[0xD012] = 0x00
        instr += 1
        if instr > MAX_INSTR:
            print("Warning: CPU executed a high number of instructions in init, breaking")
            break

    if playaddress == 0:
        print("Warning: SID has play address 0, reading from interrupt vector instead")
        if (cpu.mem[0x01] & 0x07) == 0x05:
            playaddress = cpu.mem[0xFFFE] | (cpu.mem[0xFFFF] << 8)
        else:
            playaddress = cpu.mem[0x0314] | (cpu.mem[0x0315] << 8)
        print(f"New play address is ${playaddress:04X}")

    chn = [Channel(), Channel(), Channel()]
    prevchn = [Channel(), Channel(), Channel()]
    prevchn2 = [Channel(), Channel(), Channel()]
    filt = Filter()
    prevfilt = Filter()

    total_frames = opts.seconds * 50
    print(f"Calling playroutine for {total_frames} frames, starting from frame {opts.firstframe}")
    print(f"Middle C frequency is ${freqtbllo[48] | (freqtblhi[48] << 8):04X}\n")

    header = "| Frame | Freq Note/Abs WF ADSR Pul | Freq Note/Abs WF ADSR Pul | Freq Note/Abs WF ADSR Pul | FCut RC Typ V |"
    if opts.profiling:
        header += " Cycl RL RB |"
    print(header)
    separator = "+-------+---------------------------+---------------------------+---------------------------+---------------+"
    if opts.profiling:
        separator += "------------+"
    print(separator)

    frames = 0
    counter = 0
    rows = 0
    tempoindex = 0

    while frames < opts.firstframe + total_frames:
        cpu.init(playaddress, 0, 0, 0)
        instr = 0
        while cpu.step():
            instr += 1
            if instr > MAX_INSTR:
                print("Error: CPU executed abnormally high amount of instructions in playroutine, exiting")
                return 1
            if (cpu.mem[0x01] & 0x07) != 0x05 and cpu.pc in {0xEA31, 0xEA81}:
                break

        for channel in range(3):
            base = 0xD400 + 7 * channel
            chn[channel].freq = cpu.mem[base] | (cpu.mem[base + 1] << 8)
            chn[channel].pulse = (cpu.mem[base + 2] | (cpu.mem[base + 3] << 8)) & 0x0FFF
            chn[channel].wave = cpu.mem[base + 4]
            chn[channel].adsr = cpu.mem[base + 6] | (cpu.mem[base + 5] << 8)
        filt.cutoff = (cpu.mem[0xD415] << 5) | (cpu.mem[0xD416] << 8)
        filt.ctrl = cpu.mem[0xD417]
        filt.type = cpu.mem[0xD418]

        if frames >= opts.firstframe:
            output: list[str] = []
            time = frames - opts.firstframe
            if not opts.timeseconds:
                output.append(f"| {time:5d} | ")
            else:
                output.append(f"|{time // 3000:01d}:{(time // 50) % 60:02d}.{time % 50:02d}| ")

            for channel in range(3):
                newnote = False
                cur = chn[channel]
                prev = prevchn[channel]
                prev2 = prevchn2[channel]

                if cur.wave >= 0x10 and (cur.wave & 1) and ((not (prev2.wave & 1)) or prev2.wave < 0x10):
                    prev.note = -1

                if frames == opts.firstframe or prev.note == -1 or cur.freq != prev.freq:
                    delta = cur.freq - prev2.freq
                    output.append(f"{cur.freq:04X} ")

                    if cur.wave >= 0x10:
                        dist = 0x7FFFFFFF
                        for note_index in range(96):
                            cmpfreq = freqtbllo[note_index] | (freqtblhi[note_index] << 8)
                            new_dist = abs(cur.freq - cmpfreq)
                            if new_dist < dist:
                                dist = new_dist
                                if note_index == prev.note:
                                    dist //= opts.oldnotefactor
                                cur.note = note_index

                        if cur.note != prev.note:
                            if prev.note == -1:
                                if opts.lowres:
                                    newnote = True
                                output.append(f" {NOTENAMES[cur.note]} {cur.note | 0x80:02X}  ")
                            else:
                                output.append(f"({NOTENAMES[cur.note]} {cur.note | 0x80:02X}) ")
                        elif delta:
                            if delta > 0:
                                output.append(f"(+ {delta:04X}) ")
                            else:
                                output.append(f"(- {-delta:04X}) ")
                        else:
                            output.append(" ... ..  ")
                    else:
                        output.append(" ... ..  ")
                else:
                    output.append("....  ... ..  ")

                if frames == opts.firstframe or newnote or cur.wave != prev.wave:
                    output.append(f"{cur.wave:02X} ")
                else:
                    output.append(".. ")

                if frames == opts.firstframe or newnote or cur.adsr != prev.adsr:
                    output.append(f"{cur.adsr:04X} ")
                else:
                    output.append(".... ")

                if frames == opts.firstframe or newnote or cur.pulse != prev.pulse:
                    output.append(f"{cur.pulse:03X} ")
                else:
                    output.append("... ")

                output.append("| ")

            if frames == opts.firstframe or filt.cutoff != prevfilt.cutoff:
                output.append(f"{filt.cutoff:04X} ")
            else:
                output.append(".... ")

            if frames == opts.firstframe or filt.ctrl != prevfilt.ctrl:
                output.append(f"{filt.ctrl:02X} ")
            else:
                output.append(".. ")

            if frames == opts.firstframe or ((filt.type & 0x70) != (prevfilt.type & 0x70)):
                output.append(f"{FILTERNAMES[(filt.type >> 4) & 0x07]} ")
            else:
                output.append("... ")

            if frames == opts.firstframe or ((filt.type & 0x0F) != (prevfilt.type & 0x0F)):
                output.append(f"{filt.type & 0x0F:01X} ")
            else:
                output.append(". ")

            if opts.profiling:
                cycles = cpu.cpucycles
                rasterlines = (cycles + 62) // 63
                badlines = (cycles + 503) // 504
                rasterlinesbad = (badlines * 40 + cycles + 62) // 63
                output.append(f"| {cycles:4d} {rasterlines:02X} {rasterlinesbad:02X} ")

            output.append("|\n")
            spacing = opts.spacing[tempoindex]
            if (not opts.lowres) or (spacing and not ((frames - opts.firstframe) % spacing)):
                sys.stdout.write("".join(output))
                for channel in range(3):
                    prevchn[channel] = replace(chn[channel])
                prevfilt = replace(filt)

            for channel in range(3):
                prevchn2[channel] = replace(chn[channel])

            if spacing:
                counter += 1
                if counter >= spacing:
                    tempoindex ^= 1
                    counter = 0
                    if opts.pattspacing:
                        rows += 1
                        if rows >= opts.pattspacing:
                            rows = 0
                            print("+=======+===========================+===========================+===========================+===============+")
                        elif not opts.lowres:
                            print("+-------+---------------------------+---------------------------+---------------------------+---------------+")
                    elif not opts.lowres:
                        print("+-------+---------------------------+---------------------------+---------------------------+---------------+")

        frames += 1

    return 0


def main(argv: list[str]) -> int:
    try:
        opts = parse_args(argv)
    except ValueError as exc:
        print(exc)
        return 1

    if len(argv) < 2 or opts.usage:
        print(usage(), end="")
        return 1

    try:
        return dump_sid(opts)
    except CPUError as exc:
        print(exc)
        return 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
