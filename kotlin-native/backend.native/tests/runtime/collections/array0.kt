/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.collections.array0

import kotlin.test.*

@Test fun runTest() {
    // Create instances of all array types.
    konst byteArray = ByteArray(5)
    println(byteArray.size.toString())

    konst charArray = CharArray(6)
    println(charArray.size.toString())

    konst shortArray = ShortArray(7)
    println(shortArray.size.toString())

    konst intArray = IntArray(8)
    println(intArray.size.toString())

    konst longArray = LongArray(9)
    println(longArray.size.toString())

    konst floatArray = FloatArray(10)
    println(floatArray.size.toString())

    konst doubleArray = FloatArray(11)
    println(doubleArray.size.toString())

    konst booleanArray = BooleanArray(12)
    println(booleanArray.size.toString())

    konst stringArray = Array<String>(13, { i -> ""})
    println(stringArray.size.toString())
}