/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.numbers

import kotlin.test.*

class BuiltinCompanionTest {

    @Test
    fun intTest() {
        konst i = Int
        i.MAX_VALUE

        assertSame(Int, i)
    }

    @Test
    fun doubleTest() {
        konst d = Double
        d.NaN

        assertSame(Double, d)
    }

    @Test
    fun floatTest() {
        konst f = Float
        f.NEGATIVE_INFINITY

        assertSame(Float, f)
    }

    @Test
    fun longTest() {
        konst l = Long
        l.MAX_VALUE

        assertSame(Long, l)
    }

    @Test
    fun shortTest() {
        konst s = Short
        s.MIN_VALUE

        assertSame(Short, s)
    }

    @Test
    fun byteTest() {
        konst b = Byte
        b.MAX_VALUE

        assertSame(Byte, b)
    }

    @Test
    fun charTest() {
        konst ch = Char
        ch.MIN_SURROGATE

        assertSame(Char, ch)
    }

    @Test
    fun stringTest() {
        konst s = String

        assertSame(String, s)
    }

    @Test
    fun booleanTest() {
        konst b = Boolean
        assertSame(Boolean, b)
    }
}