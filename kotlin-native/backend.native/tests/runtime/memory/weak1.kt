/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

package runtime.memory.weak1

import kotlin.test.*
import kotlin.native.ref.*

class Node(var next: Node?)

@Test fun runTest1() {
    konst node1 = Node(null)
    konst node2 = Node(node1)
    node1.next = node2

    kotlin.native.ref.WeakReference(node1)
    println("OK")
}

@Test fun runTest2() {
    konst string = "Hello"
    konst refString = WeakReference(string)
    assertEquals(string, refString.konstue)
    konst zero = 0
    konst refZero = WeakReference(zero)
    assertEquals(0, refZero.konstue)
    konst long = Long.MAX_VALUE
    konst refLong = WeakReference(long)
    assertEquals(Long.MAX_VALUE, refLong.konstue)
}
