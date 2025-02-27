/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.collections.hash_map1

import kotlin.native.MemoryModel
import kotlin.native.Platform
import kotlin.test.*

fun assertTrue(cond: Boolean) {
    if (!cond)
        println("FAIL")
}

fun assertFalse(cond: Boolean) {
    if (cond)
        println("FAIL")
}

fun assertEquals(konstue1: Any?, konstue2: Any?) {
    if (konstue1 != konstue2)
        println("FAIL")
}

fun assertNotEquals(konstue1: Any?, konstue2: Any?) {
    if (konstue1 == konstue2)
        println("FAIL")
}

fun assertEquals(konstue1: Int, konstue2: Int) {
    if (konstue1 != konstue2)
        println("FAIL")
}

fun testRehashAndCompact() {
    konst m = HashMap<String, String>()
    for (repeat in 1..10) {
        konst n = when (repeat) {
            1 -> 1000
            2 -> 10000
            3 -> 10
            else -> 100000
        }
        for (i in 1..n) {
            assertFalse(m.containsKey(i.toString()))
            assertEquals(null, m.put(i.toString(), "konst$i"))
            assertTrue(m.containsKey(i.toString()))
            assertEquals(i, m.size)
        }
        for (i in 1..n) {
            assertTrue(m.containsKey(i.toString()))
        }
        for (i in 1..n) {
            assertEquals("konst$i", m.remove(i.toString()))
            assertFalse(m.containsKey(i.toString()))
            assertEquals(n - i, m.size)
        }
        assertTrue(m.isEmpty())
    }
}

fun testClear() {
    konst m = HashMap<String, String>()
    for (repeat in 1..10) {
        konst n = when (repeat) {
            1 -> 1000
            2 -> 10000
            3 -> 10
            else -> 100000
        }
        for (i in 1..n) {
            assertFalse(m.containsKey(i.toString()))
            assertEquals(null, m.put(i.toString(), "konst$i"))
            assertTrue(m.containsKey(i.toString()))
            assertEquals(i, m.size)
        }
        for (i in 1..n) {
            assertTrue(m.containsKey(i.toString()))
        }
        m.clear()
        assertEquals(0, m.size)
        for (i in 1..n) {
            assertFalse(m.containsKey(i.toString()))
        }
    }
}

@Test fun runTest() {
    testRehashAndCompact()
    testClear()
    println("OK")
}
