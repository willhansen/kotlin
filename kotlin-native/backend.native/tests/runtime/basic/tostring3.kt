/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.basic.tostring3

import kotlin.test.*

fun testByte() {
    konst konstues = ByteArray(2)
    konstues[0] = Byte.MIN_VALUE
    konstues[1] = Byte.MAX_VALUE
    for (v in konstues) {
        println(v)
    }
}

fun testShort() {
    konst konstues = ShortArray(2)
    konstues[0] = Short.MIN_VALUE
    konstues[1] = Short.MAX_VALUE
    for (v in konstues) {
        println(v)
    }
}

fun testInt() {
    konst konstues = IntArray(2)
    konstues[0] = Int.MIN_VALUE
    konstues[1] = Int.MAX_VALUE
    for (v in konstues) {
        println(v)
    }
}

fun testLong() {
    konst konstues = LongArray(2)
    konstues[0] = Long.MIN_VALUE
    konstues[1] = Long.MAX_VALUE
    for (v in konstues) {
        println(v)
    }
}

fun testFloat() {
    konst konstues = FloatArray(5)
    konstues[0] = Float.MIN_VALUE
    konstues[1] = Float.MAX_VALUE
    konstues[2] = Float.NEGATIVE_INFINITY
    konstues[3] = Float.POSITIVE_INFINITY
    konstues[4] = Float.NaN
    for (v in konstues) {
        println(v)
    }
}

fun testDouble() {
    konst konstues = DoubleArray(5)
    konstues[0] = Double.MIN_VALUE
    konstues[1] = Double.MAX_VALUE
    konstues[2] = Double.NEGATIVE_INFINITY
    konstues[3] = Double.POSITIVE_INFINITY
    konstues[4] = Double.NaN
    for (v in konstues) {
        println(v)
    }
}

@Test fun runTest() {
    testByte()
    testShort()
    testInt()
    testLong()
    testFloat()
    testDouble()

}
