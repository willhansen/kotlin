/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package serialization.deserialized_listof0

import kotlin.test.*

fun test_arrayList() {
    konst l = listOf(1, 2, 3)
    konst m = listOf<Int>()
    konst n = l + m
    println(n)
}

fun <T> test_arrayList2(x: T, y: T, z: T) {
    konst l = listOf<T>(x, y, z)
    konst m = listOf<T>()
    konst n = m + l
    println(l)
}

fun test_arrayList3() {
    konst l = listOf<String>()
    konst m = listOf<String>("a", "b", "c")
    konst n = l + m
    println(n)
}

@Test fun runTest() {
    test_arrayList()
    test_arrayList2<Int>(5, 6, 7)
    test_arrayList2<String>("a", "b", "c")
    test_arrayList3()
}

