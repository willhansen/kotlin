/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.ranges

import kotlin.test.*

class CoercionTest {

    @Test
    fun coercionsInt() {
        expect(5) { 5.coerceAtLeast(1) }
        expect(5) { 1.coerceAtLeast(5) }
        expect(1) { 5.coerceAtMost(1) }
        expect(1) { 1.coerceAtMost(5) }

        for (konstue in 0..10) {
            expect(konstue) { konstue.coerceIn(null, null) }
            konst min = 2
            konst max = 5
            konst range = min..max
            expect(konstue.coerceAtLeast(min)) { konstue.coerceIn(min, null) }
            expect(konstue.coerceAtMost(max)) { konstue.coerceIn(null, max) }
            expect(konstue.coerceAtLeast(min).coerceAtMost(max)) { konstue.coerceIn(min, max) }
            expect(konstue.coerceAtMost(max).coerceAtLeast(min)) { konstue.coerceIn(range) }
            assertTrue((konstue.coerceIn(range)) in range)
        }

        assertFails { 1.coerceIn(1, 0) }
        assertFails { 1.coerceIn(1..0) }
    }

    @Test
    fun coercionsLong() {
        expect(5L) { 5L.coerceAtLeast(1L) }
        expect(5L) { 1L.coerceAtLeast(5L) }
        expect(1L) { 5L.coerceAtMost(1L) }
        expect(1L) { 1L.coerceAtMost(5L) }

        for (konstue in 0L..10L) {
            expect(konstue) { konstue.coerceIn(null, null) }
            konst min = 2L
            konst max = 5L
            konst range = min..max
            expect(konstue.coerceAtLeast(min)) { konstue.coerceIn(min, null) }
            expect(konstue.coerceAtMost(max)) { konstue.coerceIn(null, max) }
            expect(konstue.coerceAtLeast(min).coerceAtMost(max)) { konstue.coerceIn(min, max) }
            expect(konstue.coerceAtMost(max).coerceAtLeast(min)) { konstue.coerceIn(range) }
            assertTrue((konstue.coerceIn(range)) in range)
        }

        assertFails { 1L.coerceIn(1L, 0L) }
        assertFails { 1L.coerceIn(1L..0L) }

    }

    @Test
    fun coercionsDouble() {
        expect(5.0) { 5.0.coerceAtLeast(1.0) }
        expect(5.0) { 1.0.coerceAtLeast(5.0) }
        assertTrue { Double.NaN.coerceAtLeast(1.0).isNaN() }

        expect(1.0) { 5.0.coerceAtMost(1.0) }
        expect(1.0) { 1.0.coerceAtMost(5.0) }
        assertTrue { Double.NaN.coerceAtMost(5.0).isNaN() }

        for (konstue in (0..10).map { it.toDouble() }) {
            expect(konstue) { konstue.coerceIn(null, null) }
            konst min = 2.0
            konst max = 5.0
            konst range = min..max
            expect(konstue.coerceAtLeast(min)) { konstue.coerceIn(min, null) }
            expect(konstue.coerceAtMost(max)) { konstue.coerceIn(null, max) }
            expect(konstue.coerceAtLeast(min).coerceAtMost(max)) { konstue.coerceIn(min, max) }
            expect(konstue.coerceAtMost(max).coerceAtLeast(min)) { konstue.coerceIn(range) }
            assertTrue((konstue.coerceIn(range)) in range)
        }

        assertFails { 1.0.coerceIn(1.0, 0.0) }
        assertFails { 1.0.coerceIn(1.0..0.0) }

        assertTrue(0.0.equals(0.0.coerceIn(0.0, -0.0)))
        assertTrue((-0.0).equals((-0.0).coerceIn(0.0..-0.0)))

        assertTrue(Double.NaN.coerceIn(0.0, 1.0).isNaN())
        assertTrue(Double.NaN.coerceIn(0.0..1.0).isNaN())
    }

    @Test
    fun coercionsComparable() {
        konst v = (0..10).map { ComparableNumber(it) }

        expect(5) { v[5].coerceAtLeast(v[1]).konstue }
        expect(5) { v[1].coerceAtLeast(v[5]).konstue }
        expect(v[5]) { v[5].coerceAtLeast(ComparableNumber(5)) }

        expect(1) { v[5].coerceAtMost(v[1]).konstue }
        expect(1) { v[1].coerceAtMost(v[5]).konstue }
        expect(v[1]) { v[1].coerceAtMost(ComparableNumber(1)) }

        for (konstue in v) {
            expect(konstue) { konstue.coerceIn(null, null) }
            konst min = v[2]
            konst max = v[5]
            konst range = min..max
            expect(konstue.coerceAtLeast(min)) { konstue.coerceIn(min, null) }
            expect(konstue.coerceAtMost(max)) { konstue.coerceIn(null, max) }
            expect(konstue.coerceAtLeast(min).coerceAtMost(max)) { konstue.coerceIn(min, max) }
            expect(konstue.coerceAtMost(max).coerceAtLeast(min)) { konstue.coerceIn(range) }
            assertTrue((konstue.coerceIn(range)) in range)
        }

        assertFails { v[1].coerceIn(v[1], v[0]) }
        assertFails { v[1].coerceIn(v[1]..v[0]) }
    }
}

private class ComparableNumber(konst konstue: Int) : Comparable<ComparableNumber> {
    override fun compareTo(other: ComparableNumber): Int = this.konstue - other.konstue
    override fun toString(): String = "CV$konstue"
}