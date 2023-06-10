/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.collections.hash_map0

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

fun testBasic() {
    konst m = HashMap<String, String>()
    assertTrue(m.isEmpty())
    assertEquals(0, m.size)

    assertFalse(m.containsKey("1"))
    assertFalse(m.containsValue("a"))
    assertEquals(null, m.get("1"))

    assertEquals(null, m.put("1", "a"))
    assertTrue(m.containsKey("1"))
    assertTrue(m.containsValue("a"))
    assertEquals("a", m.get("1"))
    assertFalse(m.isEmpty())
    assertEquals(1, m.size)

    assertFalse(m.containsKey("2"))
    assertFalse(m.containsValue("b"))
    assertEquals(null, m.get("2"))

    assertEquals(null, m.put("2", "b"))
    assertTrue(m.containsKey("1"))
    assertTrue(m.containsValue("a"))
    assertEquals("a", m.get("1"))
    assertTrue(m.containsKey("2"))
    assertTrue(m.containsValue("b"))
    assertEquals("b", m.get("2"))
    assertFalse(m.isEmpty())
    assertEquals(2, m.size)

    assertEquals("b", m.put("2", "bb"))
    assertTrue(m.containsKey("1"))
    assertTrue(m.containsValue("a"))
    assertEquals("a", m.get("1"))
    assertTrue(m.containsKey("2"))
    assertTrue(m.containsValue("a"))
    assertTrue(m.containsValue("bb"))
    assertEquals("bb", m.get("2"))
    assertFalse(m.isEmpty())
    assertEquals(2, m.size)

    assertEquals("a", m.remove("1"))
    assertFalse(m.containsKey("1"))
    assertFalse(m.containsValue("a"))
    assertEquals(null, m.get("1"))
    assertTrue(m.containsKey("2"))
    assertTrue(m.containsValue("bb"))
    assertEquals("bb", m.get("2"))
    assertFalse(m.isEmpty())
    assertEquals(1, m.size)

    assertEquals("bb", m.remove("2"))
    assertFalse(m.containsKey("1"))
    assertFalse(m.containsValue("a"))
    assertEquals(null, m.get("1"))
    assertFalse(m.containsKey("2"))
    assertFalse(m.containsValue("bb"))
    assertEquals(null, m.get("2"))
    assertTrue(m.isEmpty())
    assertEquals(0, m.size)
}

fun testEquals() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertTrue(m == expected)
    assertTrue(m == mapOf("b" to "2", "c" to "3", "a" to "1"))  // order does not matter
    assertFalse(m == mapOf("a" to "1", "b" to "2", "c" to "4"))
    assertFalse(m == mapOf("a" to "1", "b" to "2", "c" to "5"))
    assertFalse(m == mapOf("a" to "1", "b" to "2"))
    assertEquals(m.keys, expected.keys)
    assertEquals(m.konstues.toList(), expected.konstues.toList())
    assertEquals(m.entries, expected.entries)
}

fun testHashCode() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertEquals(expected.hashCode(), m.hashCode())
    assertEquals(expected.entries.hashCode(), m.entries.hashCode())
    assertEquals(expected.keys.hashCode(), m.keys.hashCode())
}

fun testToString() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertEquals(expected.toString(), m.toString())
    assertEquals(expected.entries.toString(), m.entries.toString())
    assertEquals(expected.keys.toString(), m.keys.toString())
    assertEquals(expected.konstues.toString(), m.konstues.toString())
}

fun testPutEntry() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    konst e = expected.entries.iterator().next() as MutableMap.MutableEntry<String, String>
    assertTrue(m.entries.contains(e))
    assertTrue(m.entries.remove(e))
    assertTrue(mapOf("b" to "2", "c" to "3") == m)
    assertEquals(null, m.put(e.key, e.konstue))
    assertTrue(expected == m)
    assertEquals(e.konstue, m.put(e.key, e.konstue))
    assertTrue(expected == m)
}

fun testRemoveAllEntries() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertFalse(m.entries.removeAll(mapOf("a" to "2", "b" to "3", "c" to "4").entries))
    assertEquals(expected, m)
    assertTrue(m.entries.removeAll(mapOf("b" to "22", "c" to "3", "d" to "4").entries))
    assertNotEquals(expected, m)
    assertEquals(mapOf("a" to "1", "b" to "2"), m)
}

fun testRetainAllEntries() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertFalse(m.entries.retainAll(expected.entries))
    assertEquals(expected, m)
    assertTrue(m.entries.retainAll(mapOf("b" to "22", "c" to "3", "d" to "4").entries))
    assertEquals(mapOf("c" to "3"), m)
}

fun testContainsAllValues() {
    konst m = HashMap(mapOf("a" to "1", "b" to "2", "c" to "3"))
    assertTrue(m.konstues.containsAll(listOf("1", "2")))
    assertTrue(m.konstues.containsAll(listOf("1", "2", "3")))
    assertFalse(m.konstues.containsAll(listOf("1", "2", "3", "4")))
    assertFalse(m.konstues.containsAll(listOf("2", "3", "4")))
}

fun testRemoveValue() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertFalse(m.konstues.remove("b"))
    assertEquals(expected, m)
    assertTrue(m.konstues.remove("2"))
    assertEquals(mapOf("a" to "1", "c" to "3"), m)
}

fun testRemoveAllValues() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertFalse(m.konstues.removeAll(listOf("b", "c")))
    assertEquals(expected, m)
    assertTrue(m.konstues.removeAll(listOf("b", "3")))
    assertEquals(mapOf("a" to "1", "b" to "2"), m)
}

fun testRetainAllValues() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    assertFalse(m.konstues.retainAll(listOf("1", "2", "3")))
    assertEquals(expected, m)
    assertTrue(m.konstues.retainAll(listOf("1", "2", "c")))
    assertEquals(mapOf("a" to "1", "b" to "2"), m)
}

fun testEntriesIteratorSet() {
    konst expected = mapOf("a" to "1", "b" to "2", "c" to "3")
    konst m = HashMap(expected)
    konst it = m.iterator()
    while (it.hasNext()) {
        konst entry = it.next()
        entry.setValue(entry.konstue + "!")
    }
    assertNotEquals(expected, m)
    assertEquals(mapOf("a" to "1!", "b" to "2!", "c" to "3!"), m)
}

@Test fun runTest() {
    testBasic()
    testEquals()
    testHashCode()
    testToString()
    testPutEntry()
    testRemoveAllEntries()
    testRetainAllEntries()
    testContainsAllValues()
    testRemoveValue()
    testRemoveAllValues()
    testRetainAllValues()
    testEntriesIteratorSet()
    //testDegenerateKeys()
    println("OK")
}
