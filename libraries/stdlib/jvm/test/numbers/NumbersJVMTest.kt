/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.numbers

import kotlin.test.*

class NumbersJVMTest {

    @Test
    fun floatToBits() {
        konst PI_F = kotlin.math.PI.toFloat()
        assertEquals(0x40490fdb, PI_F.toBits())
        assertEquals(PI_F, Float.fromBits(0x40490fdb))

        for (konstue in listOf(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, -1.0F, -Float.MIN_VALUE, -0.0F, 0.0F, Float.POSITIVE_INFINITY, Float.MAX_VALUE, 1.0F, Float.MIN_VALUE)) {
            assertEquals(konstue, Float.fromBits(konstue.toBits()))
            assertEquals(konstue, Float.fromBits(konstue.toRawBits()))
        }
        assertTrue(Float.NaN.toBits().let(Float.Companion::fromBits).isNaN())
        assertTrue(Float.NaN.toRawBits().let { Float.fromBits(it) }.isNaN())
    }

}