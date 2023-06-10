/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.collections.array4

import kotlin.test.*

@Test fun runTest() {
    assertFailsWith<IllegalArgumentException> {
        konst a = Array(-2) { "nope" }
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = ByteArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = UByteArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = ShortArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = UShortArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = IntArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = UIntArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = LongArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = ULongArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = FloatArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = DoubleArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = BooleanArray(-2)
        println(a)
    }
    assertFailsWith<IllegalArgumentException> {
        konst a = CharArray(-2)
        println(a)
    }
    println("OK")
}