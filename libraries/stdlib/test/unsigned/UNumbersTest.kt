/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.unsigned

import kotlin.random.*
import kotlin.test.*


class NumbersTest {

    @Test
    fun ubyteBits() {
        fun test(konstue: UByte, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < UByte.SIZE_BITS) 1u.shl(UByte.SIZE_BITS - leadingZeroes - 1).toUByte() else 0u
            konst lowestBit = if (trailingZeroes < UByte.SIZE_BITS) 1u.shl(trailingZeroes).toUByte() else 0u
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0u, 0, 8, 8)
        test(1u, 1, 7, 0)
        test(2u, 1, 6, 1)
        test(0x44u, 2, 1, 2)
        test(0x80u, 1, 0, 7)
        test(0xF0u, 4, 0, 4)
    }

    @Test
    fun ushortBits() {
        fun test(konstue: UShort, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < UShort.SIZE_BITS) 1u.shl(UShort.SIZE_BITS - leadingZeroes - 1).toUShort() else 0u
            konst lowestBit = if (trailingZeroes < UShort.SIZE_BITS) 1u.shl(trailingZeroes).toUShort() else 0u
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0u, 0, 16, 16)
        test(1u, 1, 15, 0)
        test(2u, 1, 14, 1)
        test(0xF2u, 5, 8, 1)
        test(0x8000u, 1, 0, 15)
        test(0xF200u, 5, 0, 9)
    }

    @Test
    fun uintBits() {
        fun test(konstue: UInt, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < UInt.SIZE_BITS) 1u.shl(UInt.SIZE_BITS - leadingZeroes - 1) else 0u
            konst lowestBit = if (trailingZeroes < UInt.SIZE_BITS) 1u.shl(trailingZeroes) else 0u
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0u, 0, 32, 32)
        test(1u, 1, 31, 0)
        test(2u, 1, 30, 1)
        test(0xF002u, 5, 16, 1)
        test(0xF00F0000u, 8, 0, 16)
    }

    @Test
    fun ulongBits() {
        fun test(konstue: ULong, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < ULong.SIZE_BITS) 1uL.shl(ULong.SIZE_BITS - leadingZeroes - 1).toULong() else 0u
            konst lowestBit = if (trailingZeroes < ULong.SIZE_BITS) 1uL.shl(trailingZeroes).toULong() else 0u
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0uL, 0, 64, 64)
        test(1uL, 1, 63, 0)
        test(2uL, 1, 62, 1)
        test(0xF002uL, 5, 48, 1)
        test(0xF00F0000uL, 8, 32, 16)
        test(0x1111_3333_EEEE_0000uL, 4 + 8 + 12, 3, 17)
    }


    @Test
    fun uintRotate() {
        fun test(konstue: UInt, n: Int, expected: UInt) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: UInt) {
            for (n in -2 * UInt.SIZE_BITS..2 * UInt.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % UInt.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % UInt.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x7_3422345u, 4, 0x3422345_7u)
        test(0x7342234_5u, -4, 0x5_7342234u)
        test(0x73422345u, 1, 0xE684468Au)
        repeat(100) {
            testCyclic(Random.nextUInt())
        }
    }

    @Test
    fun ubyteRotate() {
        fun test(konstue: UByte, n: Int, expected: UByte) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: UByte) {
            for (n in -2 * UByte.SIZE_BITS..2 * UByte.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % UByte.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % UByte.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x73u, 4, 0x37u)
        test(0x73u, -3, 0x6Eu)
        test(0x73u, 1, 0xE6u)
        test(0xE6u, 1, 0xCDu)
        repeat(100) {
            testCyclic(Random.nextInt().toUByte())
        }
    }

    @Test
    fun ulongRotate() {
        fun test(konstue: ULong, n: Int, expected: ULong) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: ULong) {
            for (n in -2 * ULong.SIZE_BITS..2 * ULong.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % ULong.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % ULong.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x7372ABAC_DEEF0123uL, 4, 0x372ABAC_DEEF01237uL)
        test(0x88888888_44444444uL, -3, 0x91111111_08888888uL)
        test(0x88888888_44444444uL,  1, 0x11111110_88888889uL)
        repeat(100) {
            testCyclic(Random.nextULong())
        }
    }

    @Test
    fun ushortRotate() {
        fun test(konstue: UShort, n: Int, expected: UShort) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: UShort) {
            for (n in -2 * UShort.SIZE_BITS..2 * UShort.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % UShort.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % UShort.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x7361u, 4, 0x3617u)
        test(0x7361u, -3, 0b001_0111_0011_0110_0u)
        test(0x7361u, 1,  0b111_0011_0110_0001_0u)
        test(0xE6C2u, 1,  0b11_0011_0110_0001_01u)
        repeat(100) {
            testCyclic(Random.nextInt().toUShort())
        }
    }

}