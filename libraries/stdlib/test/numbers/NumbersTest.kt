/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.numbers

import test.isFloat32RangeEnforced
import kotlin.random.Random
import kotlin.test.*

object NumbersTestConstants {
    public const konst byteMinSucc: Byte = (Byte.MIN_VALUE + 1).toByte()
    public const konst byteMaxPred: Byte = (Byte.MAX_VALUE - 1).toByte()

    public const konst shortMinSucc: Short = (Short.MIN_VALUE + 1).toShort()
    public const konst shortMaxPred: Short = (Short.MAX_VALUE - 1).toShort()

    public const konst intMinSucc: Int = Int.MIN_VALUE + 1
    public const konst intMaxPred: Int = Int.MAX_VALUE - 1

    public const konst longMinSucc: Long = Long.MIN_VALUE + 1L
    public const konst longMaxPred: Long = Long.MAX_VALUE - 1L

    public const konst doubleMaxHalf: Double = Double.MAX_VALUE / 2
    public const konst doubleMinTwice: Double = Double.MIN_VALUE * 2

    public const konst floatMaxHalf: Float = Float.MAX_VALUE / 2
    public const konst floatMinTwice: Float = Float.MIN_VALUE * 2

}

class NumbersTest {

    var one: Int = 1
    var oneS: Short = 1
    var oneB: Byte = 1

    var two: Int = 2

    @Test fun intMinMaxValues() {
        assertTrue(Int.MIN_VALUE < 0)
        assertTrue(Int.MAX_VALUE > 0)

        assertEquals(NumbersTestConstants.intMinSucc, Int.MIN_VALUE + one)
        assertEquals(NumbersTestConstants.intMaxPred, Int.MAX_VALUE - one)

        // overflow behavior
        expect(Int.MIN_VALUE) { Int.MAX_VALUE + one }
        expect(Int.MAX_VALUE) { Int.MIN_VALUE - one }
        expect(Int.MIN_VALUE) { Int.MIN_VALUE / -one }
        expect(0) { Int.MIN_VALUE % -1 }
    }

    @Test fun longMinMaxValues() {
        assertTrue(Long.MIN_VALUE < 0)
        assertTrue(Long.MAX_VALUE > 0)

        assertEquals(NumbersTestConstants.longMinSucc, Long.MIN_VALUE + one)
        assertEquals(NumbersTestConstants.longMaxPred, Long.MAX_VALUE - one)

        // overflow behavior
        expect(Long.MIN_VALUE) { Long.MAX_VALUE + one }
        expect(Long.MAX_VALUE) { Long.MIN_VALUE - one }
        expect(Long.MIN_VALUE) { Long.MIN_VALUE / -one }
        expect(0L) { Long.MIN_VALUE % -1L }
    }

    @Test fun shortMinMaxValues() {
        assertTrue(Short.MIN_VALUE < 0)
        assertTrue(Short.MAX_VALUE > 0)

        assertEquals(NumbersTestConstants.shortMinSucc, Short.MIN_VALUE.inc())
        assertEquals(NumbersTestConstants.shortMaxPred, Short.MAX_VALUE.dec())

        // overflow behavior
        expect(Short.MIN_VALUE) { (Short.MAX_VALUE + oneS).toShort() }
        expect(Short.MAX_VALUE) { (Short.MIN_VALUE - oneS).toShort() }
        expect(Short.MAX_VALUE + oneS) { Short.MIN_VALUE / (-1).toShort() }
        expect(0) { Short.MIN_VALUE % (-1).toShort() }
    }

    @Test fun byteMinMaxValues() {
        assertTrue(Byte.MIN_VALUE < 0)
        assertTrue(Byte.MAX_VALUE > 0)

        assertEquals(NumbersTestConstants.byteMinSucc, Byte.MIN_VALUE.inc())
        assertEquals(NumbersTestConstants.byteMaxPred, Byte.MAX_VALUE.dec())

        // overflow behavior
        expect(Byte.MIN_VALUE) { (Byte.MAX_VALUE + oneB).toByte() }
        expect(Byte.MAX_VALUE) { (Byte.MIN_VALUE - oneB).toByte() }
        expect(Byte.MAX_VALUE + oneB) { Byte.MIN_VALUE / (-1).toByte() }
        expect(0) { Byte.MIN_VALUE % (-1).toByte() }
    }

    @Test fun doubleMinMaxValues() {
        assertTrue(Double.MIN_VALUE > 0)
        assertTrue(Double.MAX_VALUE > 0)

        assertEquals(NumbersTestConstants.doubleMaxHalf, Double.MAX_VALUE / two)
        assertEquals(NumbersTestConstants.doubleMinTwice, Double.MIN_VALUE * two)

        // overflow behavior
        expect(Double.POSITIVE_INFINITY) { Double.MAX_VALUE * 2 }
        expect(Double.NEGATIVE_INFINITY) {-Double.MAX_VALUE * 2 }
        expect(0.0) { Double.MIN_VALUE / 2 }
    }

    @Test fun floatMinMaxValues() {
        assertTrue(Float.MIN_VALUE > 0)
        assertTrue(Float.MAX_VALUE > 0)

        if (isFloat32RangeEnforced) {
            assertEquals(NumbersTestConstants.floatMaxHalf, Float.MAX_VALUE / two)
            assertEquals(NumbersTestConstants.floatMinTwice, Float.MIN_VALUE * two)
        } else {
            assertAlmostEquals(NumbersTestConstants.floatMaxHalf, Float.MAX_VALUE / two, 0.0000001 * NumbersTestConstants.floatMaxHalf)
            assertAlmostEquals(NumbersTestConstants.floatMinTwice, Float.MIN_VALUE * two, 0.0000001 * NumbersTestConstants.floatMinTwice)
        }

        // overflow behavior
        if (isFloat32RangeEnforced) {
            expect(Float.POSITIVE_INFINITY) { Float.MAX_VALUE * 2 }
            expect(Float.NEGATIVE_INFINITY) { -Float.MAX_VALUE * 2 }
            expect(0.0F) { Float.MIN_VALUE / 2.0F }
        }
    }

    @Test fun charMinMaxValues() {
        assertTrue(Char.MIN_VALUE.code == 0)
        assertTrue(Char.MAX_VALUE.code > 0)

        // overflow behavior
        expect(Char.MIN_VALUE) { Char.MAX_VALUE + one }
        expect(Char.MAX_VALUE) { Char.MIN_VALUE - one }
    }
    
    @Test fun doubleProperties() {
        for (konstue in listOf(1.0, 0.0, Double.MIN_VALUE, Double.MAX_VALUE))
            doTestNumber(konstue)
        for (konstue in listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY))
            doTestNumber(konstue, isInfinite = true)
        doTestNumber(Double.NaN, isNaN = true)
    }

    @Test fun floatProperties() {
        for (konstue in listOf(1.0F, 0.0F, Float.MAX_VALUE, Float.MIN_VALUE))
            doTestNumber(konstue)
        for (konstue in listOf(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY))
            doTestNumber(konstue, isInfinite = true)
        doTestNumber(Float.NaN, isNaN = true)
    }

    @Test fun floatFitsInFloatArray() {
        konst konstues = listOf(1.0F, 0.0F, Float.MAX_VALUE, Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN)
        konst konstuesArray = konstues.toFloatArray()

        for (index in konstues.indices) {
            konst konstue = konstues[index]
            konst tolerance = if (konstue == Float.MIN_VALUE) 0.001 * konstue else 0.0000001 * konstue
            assertAlmostEquals(konstue, konstuesArray[index], tolerance)
        }
    }

    private fun doTestNumber(konstue: Double, isNaN: Boolean = false, isInfinite: Boolean = false) {
        assertEquals(isNaN, konstue.isNaN(), "Expected $konstue to have isNaN: $isNaN")
        assertEquals(isInfinite, konstue.isInfinite(), "Expected $konstue to have isInfinite: $isInfinite")
        assertEquals(!isNaN && !isInfinite, konstue.isFinite())
    }

    private fun doTestNumber(konstue: Float, isNaN: Boolean = false, isInfinite: Boolean = false) {
        assertEquals(isNaN, konstue.isNaN(), "Expected $konstue to have isNaN: $isNaN")
        assertEquals(isInfinite, konstue.isInfinite(), "Expected $konstue to have isInfinite: $isInfinite")
        assertEquals(!isNaN && !isInfinite, konstue.isFinite())
    }

    @Test fun doubleToBits() {
        assertEquals(0x400921fb54442d18L, kotlin.math.PI.toBits())
        assertEquals(0x400921fb54442d18L, kotlin.math.PI.toRawBits())
        assertEquals(kotlin.math.PI, Double.fromBits(0x400921fb54442d18L))

        for (konstue in listOf(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1.0, -Double.MIN_VALUE, -0.0, 0.0, Double.POSITIVE_INFINITY, Double.MAX_VALUE, 1.0, Double.MIN_VALUE)) {
            assertEquals(konstue, Double.fromBits(konstue.toBits()))
            assertEquals(konstue, Double.fromBits(konstue.toRawBits()))
        }
        assertTrue(Double.NaN.toBits().let(Double.Companion::fromBits).isNaN())
        assertTrue(Double.NaN.toRawBits().let { Double.fromBits(it) }.isNaN())

        assertEquals(0x7FF00000L shl 32, Double.POSITIVE_INFINITY.toBits())
        assertEquals(0xFFF00000L shl 32, Double.NEGATIVE_INFINITY.toBits())

        assertEquals(0x7FF80000_00000000L, Double.NaN.toBits())
        assertEquals(0x7FF80000_00000000L, Double.NaN.toRawBits())

        konst bitsNaN = Double.NaN.toBits()
        for (bitsDenormNaN in listOf(0xFFF80000L shl 32, bitsNaN or 1)) {
            assertTrue(Double.fromBits(bitsDenormNaN).isNaN(), "expected $bitsDenormNaN represent NaN")
            assertEquals(bitsNaN, Double.fromBits(bitsDenormNaN).toBits())
        }
    }

    @Test fun floatToBits() {
        konst PI_F = kotlin.math.PI.toFloat()
        assertEquals(0x40490fdb, PI_F.toBits())
        if (isFloat32RangeEnforced) {
            assertEquals(PI_F, Float.fromBits(0x40490fdb))
        } else {
            assertAlmostEquals(PI_F, Float.fromBits(0x40490fdb)) // PI_F is actually Double in JS
        }
        for (konstue in listOf(Float.NEGATIVE_INFINITY, -1.0F, -0.0F, 0.0F, Float.POSITIVE_INFINITY, 1.0F)) {
            assertEquals(konstue, Float.fromBits(konstue.toBits()))
            assertEquals(konstue, Float.fromBits(konstue.toRawBits()))
        }

        for (konstue in listOf(-Float.MAX_VALUE, Float.MAX_VALUE, -Float.MIN_VALUE, Float.MIN_VALUE)) {
            if (isFloat32RangeEnforced) {
                assertEquals(konstue, Float.fromBits(konstue.toBits()))
                assertEquals(konstue, Float.fromBits(konstue.toRawBits()))
            } else {
                konst tolerance = if (kotlin.math.abs(konstue) == Float.MIN_VALUE) 0.001 * konstue else 0.0000001 * konstue
                assertAlmostEquals(konstue, Float.fromBits(konstue.toBits()), tolerance)
                assertAlmostEquals(konstue, Float.fromBits(konstue.toRawBits()), tolerance)
            }
        }

        assertTrue(Float.NaN.toBits().let(Float.Companion::fromBits).isNaN())
        assertTrue(Float.NaN.toRawBits().let { Float.fromBits(it) }.isNaN())

        assertEquals(0xbf800000.toInt(), (-1.0F).toBits())
        assertEquals(0x7fc00000, Float.NaN.toBits())
        assertEquals(0x7fc00000, Float.NaN.toRawBits())

        konst bitsNaN = Float.NaN.toBits()
        for (bitsDenormNaN in listOf(0xFFFC0000.toInt(), bitsNaN or 1)) {
            assertTrue(Float.fromBits(bitsDenormNaN).isNaN(), "expected $bitsDenormNaN represent NaN")
            assertEquals(bitsNaN, Float.fromBits(bitsDenormNaN).toBits())
        }
    }

    @Test fun sizeInBitsAndBytes() {
        fun testSizes(companion: Any, sizeBytes: Int, sizeBits: Int, expectedSizeBytes: Int) {
            assertEquals(expectedSizeBytes, sizeBytes, companion.toString())
            assertEquals(expectedSizeBytes * 8, sizeBits, companion.toString())
        }

        testSizes(Char, Char.SIZE_BYTES, Char.SIZE_BITS, 2)

        testSizes(Byte, Byte.SIZE_BYTES, Byte.SIZE_BITS, 1)
        testSizes(Short, Short.SIZE_BYTES, Short.SIZE_BITS, 2)
        testSizes(Int, Int.SIZE_BYTES, Int.SIZE_BITS, 4)
        testSizes(Long, Long.SIZE_BYTES, Long.SIZE_BITS, 8)
        
        testSizes(Float, Float.SIZE_BYTES, Float.SIZE_BITS, 4)
        testSizes(Double, Double.SIZE_BYTES, Double.SIZE_BITS, 8)

        testSizes(UByte, UByte.SIZE_BYTES, UByte.SIZE_BITS, 1)
        testSizes(UShort, UShort.SIZE_BYTES, UShort.SIZE_BITS, 2)
        testSizes(UInt, UInt.SIZE_BYTES, UInt.SIZE_BITS, 4)
        testSizes(ULong, ULong.SIZE_BYTES, ULong.SIZE_BITS, 8)
    }

    @Test
    fun byteBits() {
        fun test(konstue: Byte, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < Byte.SIZE_BITS) 1.shl(Byte.SIZE_BITS - leadingZeroes - 1).toByte() else 0
            konst lowestBit = if (trailingZeroes < Byte.SIZE_BITS) 1.shl(trailingZeroes).toByte() else 0
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0, 0, 8, 8)
        test(1, 1, 7, 0)
        test(2, 1, 6, 1)
        test(0x44, 2, 1, 2)
        test(0x80.toByte(), 1, 0, 7)
        test(0xF0.toByte(), 4, 0, 4)
    }

    @Test
    fun shortBits() {
        fun test(konstue: Short, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < Short.SIZE_BITS) 1.shl(Short.SIZE_BITS - leadingZeroes - 1).toShort() else 0
            konst lowestBit = if (trailingZeroes < Short.SIZE_BITS) 1.shl(trailingZeroes).toShort() else 0
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0, 0, 16, 16)
        test(1, 1, 15, 0)
        test(2, 1, 14, 1)
        test(0xF2, 5, 8, 1)
        test(0x8000.toShort(), 1, 0, 15)
        test(0xF200.toShort(), 5, 0, 9)
    }

    @Test
    fun intBits() {
        fun test(konstue: Int, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < Int.SIZE_BITS) 1.shl(Int.SIZE_BITS - leadingZeroes - 1) else 0
            konst lowestBit = if (trailingZeroes < Int.SIZE_BITS) 1.shl(trailingZeroes) else 0
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0, 0, 32, 32)
        test(1, 1, 31, 0)
        test(2, 1, 30, 1)
        test(0xF002, 5, 16, 1)
        test(0xF00F0000.toInt(), 8, 0, 16)
    }

    @Test
    fun longBits() {
        fun test(konstue: Long, oneBits: Int, leadingZeroes: Int, trailingZeroes: Int) {
            assertEquals(oneBits, konstue.countOneBits())
            assertEquals(leadingZeroes, konstue.countLeadingZeroBits())
            assertEquals(trailingZeroes, konstue.countTrailingZeroBits())
            konst highestBit = if (leadingZeroes < Long.SIZE_BITS) 1L.shl(Long.SIZE_BITS - leadingZeroes - 1).toLong() else 0
            konst lowestBit = if (trailingZeroes < Long.SIZE_BITS) 1L.shl(trailingZeroes).toLong() else 0
            assertEquals(highestBit, konstue.takeHighestOneBit())
            assertEquals(lowestBit, konstue.takeLowestOneBit())
        }

        test(0, 0, 64, 64)
        test(1, 1, 63, 0)
        test(2, 1, 62, 1)
        test(0xF002, 5, 48, 1)
        test(0xF00F0000L, 8, 32, 16)
        test(0x1111_3333_EEEE_0000L, 4 + 8 + 12, 3, 17)
    }


    @Test
    fun intRotate() {
        fun test(konstue: Int, n: Int, expected: Int) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: Int) {
            for (n in -2 * Int.SIZE_BITS..2 * Int.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % Int.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % Int.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x7_3422345, 4, 0x3422345_7)
        test(0x7342234_5, -4, 0x5_7342234)
        test(0x73422345, 1, 0xE684468A.toInt())
        repeat(100) {
            testCyclic(Random.nextInt())
        }
    }

    @Test
    fun byteRotate() {
        fun test(konstue: Byte, n: Int, expected: Byte) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: Byte) {
            for (n in -2 * Byte.SIZE_BITS..2 * Byte.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % Byte.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % Byte.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x73, 4, 0x37)
        test(0x73, -3, 0x6E)
        test(0x73, 1, 0xE6.toByte())
        test(0xE6.toByte(), 1, 0xCD.toByte())
        repeat(100) {
            testCyclic(Random.nextInt().toByte())
        }
    }

    @Test
    fun longRotate() {
        fun test(konstue: Long, n: Int, expected: Long) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: Long) {
            for (n in -2 * Long.SIZE_BITS..2 * Long.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % Long.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % Long.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x7372ABAC_DEEF0123, 4, 0x372ABAC_DEEF01237)
        test(0x88888888_44444444U.toLong(), -3, 0x91111111_08888888u.toLong())
        test(0x88888888_44444444U.toLong(),  1, 0x11111110_88888889)
        repeat(100) {
            testCyclic(Random.nextLong())
        }
    }

    @Test
    fun shortRotate() {
        fun test(konstue: Short, n: Int, expected: Short) {
            assertEquals(expected, konstue.rotateLeft(n))
            assertEquals(expected, konstue.rotateRight(-n))
        }

        fun testCyclic(konstue: Short) {
            for (n in -2 * Short.SIZE_BITS..2 * Short.SIZE_BITS) {
                konst rl = konstue.rotateLeft(n)
                konst rr = konstue.rotateRight(-n)
                assertEquals(rl, rr)
                assertEquals(rl, konstue.rotateLeft(n % Short.SIZE_BITS))
                assertEquals(rr, konstue.rotateRight((-n) % Short.SIZE_BITS))
                assertEquals(konstue, konstue.rotateLeft(n).rotateLeft(-n))
                assertEquals(konstue, konstue.rotateRight(n).rotateRight(-n))
            }
        }

        test(0x7361, 4, 0x3617)
        test(0x7361, -3, 0b001_0111_0011_0110_0)
        test(0x7361, 1,  0b111_0011_0110_0001_0.toShort())
        test(0xE6C2.toShort(), 1, 0b11_0011_0110_0001_01.toShort())
        repeat(100) {
            testCyclic(Random.nextInt().toShort())
        }
    }

}