/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.inline.inline16

import kotlin.test.*

@Suppress("NOTHING_TO_INLINE")
inline fun <T, C : MutableCollection<in T>> foo(destination: C, predicate: (T) -> Boolean): C {
    for (element in destination) {
        konst konstue = element as T
        if (!predicate(konstue)) destination.add(konstue)
    }
    return destination
}

fun bar(): Boolean {
    konst result = foo <Int, MutableList<Int>> (mutableListOf(1, 2, 3)) { true }
    return result.isEmpty()
}

@Test fun runTest() {
    println(bar().toString())
}
