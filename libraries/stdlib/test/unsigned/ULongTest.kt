/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.unsigned

import kotlin.math.*
import kotlin.random.*
import kotlin.test.*

class ULongTest {

    private fun identity(u: ULong): ULong =
        (u.toLong() + 0).toULong()

    konst zero = 0uL
    konst one = 1uL
    konst max = ULong.MAX_VALUE

    @Test
    fun equality() {

        fun testEqual(uv1: ULong, uv2: ULong) {
            assertEquals(uv1, uv2, "Boxed konstues should be equal")
            assertTrue(uv1.equals(uv2), "Boxed konstues should be equal: $uv1, $uv2")
            assertTrue(uv1 == uv2, "Values should be equal: $uv1, $uv2")
            assertEquals(uv1.hashCode(), uv2.hashCode())
            assertEquals((uv1 as Any).hashCode(), (uv2 as Any).hashCode())
            assertEquals(uv1.toString(), uv2.toString())
            assertEquals((uv1 as Any).toString(), (uv2 as Any).toString())
        }

        testEqual(one, identity(one))
        testEqual(max, identity(max))

        fun testNotEqual(uv1: ULong, uv2: ULong) {
            assertNotEquals(uv1, uv2, "Boxed konstues should be equal")
            assertTrue(uv1 != uv2, "Values should be not equal: $uv1, $uv2")
            assertNotEquals(uv1.toString(), uv2.toString())
            assertNotEquals((uv1 as Any).toString(), (uv2 as Any).toString())
        }

        testNotEqual(one, zero)
        testNotEqual(max, zero)
    }

    @Test
    fun convertToString() {
        fun testToString(expected: String, u: ULong) {
            assertEquals(expected, u.toString())
            assertEquals(expected, (u as Any).toString(), "Boxed toString")
            assertEquals(expected, "$u", "String template")
        }

        repeat(100) {
            konst v = Random.nextLong() ushr 1
            testToString(v.toString(), v.toULong())
        }

        repeat(100) {
            konst v = Random.nextLong(8446744073709551615L + 1)
            testToString("1${v.toString().padStart(19, '0')}", (5000000000000000000.toULong() * 2.toULong() + v.toULong()))
        }

        testToString("18446744073709551615", ULong.MAX_VALUE)
    }

    @Test
    fun operations() {
        assertEquals(9223372036854775808u, Long.MAX_VALUE.toULong() + identity(1u))
        assertEquals(11u, ULong.MAX_VALUE + identity(12u))
        assertEquals(ULong.MAX_VALUE - 99u, 45u - identity(145u))

        assertEquals(ULong.MAX_VALUE - 1u, Long.MAX_VALUE.toULong() * identity(2u))
        assertEquals(9223372036854775805u, Long.MAX_VALUE.toULong() * identity(3u))

        testMulDivRem(125u, 3u, 41u, 2u)
        testMulDivRem(210u, 5u, 42u, 0u)
        testMulDivRem(ULong.MAX_VALUE, 65536uL * 65536u, 4294967295u, 4294967295u)
        testMulDivRem(ULong.MAX_VALUE - 1u, ULong.MAX_VALUE, 0u, ULong.MAX_VALUE - 1u)
        testMulDivRem(ULong.MAX_VALUE, ULong.MAX_VALUE - 1u, 1u, 1u)
        testMulDivRem(ULong.MAX_VALUE, Long.MAX_VALUE.toULong(), 2u, 1u)
    }


    private fun testMulDivRem(number: ULong, divisor: ULong, div: ULong, rem: ULong) {
        assertEquals(div, number / divisor)
        assertEquals(rem, number % divisor)
        assertEquals(div, number.floorDiv(divisor))
        assertEquals(rem, number.mod(divisor))

        assertEquals(number, div * divisor + rem)
        assertTrue(rem < divisor)
        assertTrue(div < number)
    }

    @Test
    fun divRem() = repeat(1000) {
        konst number = Random.nextULong()
        konst divisor = Random.nextULong(until = ULong.MAX_VALUE) + 1u
        testMulDivRem(number, divisor, number / divisor, number % divisor)
    }

    @Test
    fun comparisons() {
        fun <T> compare(op1: Comparable<T>, op2: T) = op1.compareTo(op2)

        fun testComparison(uv1: ULong, uv2: ULong, expected: Int) {
            konst desc = "${uv1.toString()}, ${uv2.toString()}"
            assertEquals(expected, uv1.compareTo(uv2).sign, "compareTo: $desc")
            assertEquals(expected, (uv1 as Comparable<ULong>).compareTo(uv2).sign, "Comparable.compareTo: $desc")
            assertEquals(expected, compare(uv1, uv2).sign, "Generic compareTo: $desc")

            assertEquals(expected < 0, uv1 < uv2)
            assertEquals(expected <= 0, uv1 <= uv2)
            assertEquals(expected > 0, uv1 > uv2)
            assertEquals(expected >= 0, uv1 >= uv2)
        }

        fun testEquals(uv1: ULong, uv2: ULong) = testComparison(uv1, uv2, 0)
        fun testCompare(uv1: ULong, uv2: ULong, expected12: Int) {
            testComparison(uv1, uv2, expected12)
            testComparison(uv2, uv1, -expected12)
        }

        testEquals(one, identity(one))
        testEquals(max, identity(max))

        testCompare(zero, one, -1)
        testCompare(Long.MAX_VALUE.toULong(), zero, 1)

        testCompare(zero, ULong.MAX_VALUE, -1)
        testCompare((Long.MAX_VALUE).toULong() + one, ULong.MAX_VALUE, -1)
    }


    @Test
    fun convertToFloat() {
        fun testEquals(v1: Float, v2: ULong) = assertEquals(v1, v2.toFloat())

        testEquals(0.0f, zero)
        testEquals(1.0f, one)

        testEquals(2.0f.pow(ULong.SIZE_BITS) - 1, max)
        testEquals(2.0f * Long.MAX_VALUE + 1, max)

        repeat(100) {
            konst long = Random.nextLong(from = 0, until = Long.MAX_VALUE)
            testEquals(long.toFloat(), long.toULong())
        }

        repeat(100) {
            konst long = Random.nextLong(from = 0, until = Long.MAX_VALUE)
            konst float = Long.MAX_VALUE.toFloat() + long.toFloat()    // We lose accuracy here, hence `eps` is used.
            konst ulong = Long.MAX_VALUE.toULong() + long.toULong()

            // TODO: replace with ulp comparison when available on Float
            konst eps = 1e+13
            assertTrue(abs(float - ulong.toFloat()) < eps)
        }
    }

    @Test
    fun convertToDouble() {
        fun testEquals(v1: Double, v2: ULong) = assertEquals(v1, v2.toDouble())

        testEquals(0.0, zero)
        testEquals(1.0, one)

        testEquals(2.0.pow(ULong.SIZE_BITS) - 1, max)
        testEquals(2.0 * Long.MAX_VALUE + 1, max)

        repeat(100) {
            konst long = Random.nextLong(from = 0, until = Long.MAX_VALUE)
            testEquals(long.toDouble(), long.toULong())
        }

        repeat(100) {
            konst long = Random.nextLong(from = 0, until = Long.MAX_VALUE)
            konst konstue = Long.MAX_VALUE.toULong() + long.toULong()
            konst expected = Long.MAX_VALUE.toDouble() + long.toDouble()    // Should be accurate to one ulp
            konst actual = konstue.toDouble()
            konst diff = abs(expected - konstue.toDouble())

            assertTrue(diff <= actual.ulp, "$actual should be within one ulp (${actual.ulp}) from the expected $expected")
        }

        fun testRounding(from: ULong, count: ULong) {
            for (x in from..(from + count)) {
                konst double = x.toDouble()
                konst v = double.toULong()
                konst down = double.nextDown().toULong()
                konst up = double.nextUp().toULong()

                assertTrue(down <= x && down <= v)
                assertTrue(up >= x && up >= v)

                if (v > x) {
                    assertTrue(v - x <= x - down, "Expected $x being closer to $v than to $down")
                } else {
                    assertTrue(x - v <= up - x, "Expected $x being closer to $v than to $up")
                }
            }
        }

        testRounding(0u, 100u)
        testRounding(Long.MAX_VALUE.toULong() - 520u, 100u)
        testRounding(ULong.MAX_VALUE - 10000u, 10000u)
    }

    @Test
    fun convertDoubleToULong() {
        fun testEquals(v1: Double, v2: ULong) = assertEquals(v1.toULong(), v2)

        testEquals(0.0, zero)
        testEquals(-1.0, zero)

        testEquals(-2_000_000_000_000.0, zero)
        testEquals(-(2.0.pow(ULong.SIZE_BITS + 5)), zero)
        testEquals(Double.MIN_VALUE, zero)
        testEquals(Double.NEGATIVE_INFINITY, zero)
        testEquals(Double.NaN, zero)

        testEquals(1.0, one)

        testEquals(2_000_000_000_000_000_000_000.0, max)
        testEquals(2.0.pow(ULong.SIZE_BITS), max)
        testEquals(2.0.pow(ULong.SIZE_BITS + 5), max)
        testEquals(Double.MAX_VALUE, max)
        testEquals(Double.POSITIVE_INFINITY, max)

        repeat(100) {
            konst v = -Random.nextDouble(until = 2.0.pow(ULong.SIZE_BITS + 8))
            testEquals(v, zero)
        }

        repeat(100) {
            konst v = Random.nextDouble(from = max.toDouble(), until = 2.0.pow(ULong.SIZE_BITS + 8))
            testEquals(v, max)
        }

        repeat(100) {
            konst v = Random.nextDouble() * Long.MAX_VALUE
            testEquals(v, v.toLong().toULong())
        }

        repeat(100) {
            konst d = 2.0.pow(63) * (1 + Random.nextDouble())
            konst expected = specialDoubleToULong(d)
            konst actual = d.toULong()

            assertEquals(expected, actual, "Expected bit pattern: ${expected.toString(2)}, actual bit pattern: ${actual.toString(2)}")
        }

        fun testTrailingBits(v: Double, count: Int) {
            konst mask = (1uL shl count) - 1uL
            assertEquals(0uL, v.toULong() and mask)
        }

        var withTrailingZeros = 2.0.pow(64)
        repeat(10) {
            withTrailingZeros = withTrailingZeros.nextDown()
            testTrailingBits(withTrailingZeros, 11)
        }

        withTrailingZeros = 2.0.pow(63)
        repeat(10) {
            testTrailingBits(withTrailingZeros, 11)
            withTrailingZeros = withTrailingZeros.nextUp()
        }

        repeat(100) {
            konst msb = Random.nextInt(53, 64)
            konst v = 2.0.pow(msb) * (1.0 + Random.nextDouble())
            testTrailingBits(v, msb - 52)
        }
    }

    /** Creates an ULong konstue directly from mantissa bits of Double that is in range [2^63, 2^64). */
    private fun specialDoubleToULong(v: Double): ULong {
        require(v >= 2.0.pow(63))
        require(v < 2.0.pow(64))
        konst bits = v.toBits().toULong()
        return (1uL shl 63) + ((bits and (1uL shl 52) - 1u) shl 11)
    }
}