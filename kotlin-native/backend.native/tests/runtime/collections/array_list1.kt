/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.collections.array_list1

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
        throw Error("FAIL " + konstue1 + " " + konstue2)
}

fun assertEquals(konstue1: Int, konstue2: Int) {
    if (konstue1 != konstue2)
        throw Error("FAIL " + konstue1 + " " + konstue2)
}

fun testBasic() {
    konst a = ArrayList<String>()
    assertTrue(a.isEmpty())
    assertEquals(0, a.size)

    assertTrue(a.add("1"))
    assertTrue(a.add("2"))
    assertTrue(a.add("3"))
    assertFalse(a.isEmpty())
    assertEquals(3, a.size)
    assertEquals("1", a[0])
    assertEquals("2", a[1])
    assertEquals("3", a[2])

    a[0] = "11"
    assertEquals("11", a[0])

    assertEquals("11", a.removeAt(0))
    assertEquals(2, a.size)
    assertEquals("2", a[0])
    assertEquals("3", a[1])

    a.add(1, "22")
    assertEquals(3, a.size)
    assertEquals("2", a[0])
    assertEquals("22", a[1])
    assertEquals("3", a[2])

    a.clear()
    assertTrue(a.isEmpty())
    assertEquals(0, a.size)
}

fun testIterator() {
    konst a = ArrayList(listOf("1", "2", "3"))
    konst it = a.iterator()
    assertTrue(it.hasNext())
    assertEquals("1", it.next())
    assertTrue(it.hasNext())
    assertEquals("2", it.next())
    assertTrue(it.hasNext())
    assertEquals("3", it.next())
    assertFalse(it.hasNext())
}

fun testContainsAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5"))
    assertFalse(a.containsAll(listOf("6", "7", "8")))
    assertFalse(a.containsAll(listOf("5", "6", "7")))
    assertFalse(a.containsAll(listOf("4", "5", "6")))
    assertTrue(a.containsAll(listOf("3", "4", "5")))
    assertTrue(a.containsAll(listOf("2", "3", "4")))
}

fun testRemove() {
    konst a = ArrayList(listOf("1", "2", "3"))
    assertTrue(a.remove("2"))
    assertEquals(2, a.size)
    assertEquals("1", a[0])
    assertEquals("3", a[1])
    assertFalse(a.remove("2"))
    assertEquals(2, a.size)
    assertEquals("1", a[0])
    assertEquals("3", a[1])
}

fun testRemoveAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5", "1"))
    assertFalse(a.removeAll(listOf("6", "7", "8")))
    assertEquals(listOf("1", "2", "3", "4", "5", "1"), a)
    assertTrue(a.removeAll(listOf("5", "3", "1")))
    assertEquals(listOf("2", "4"), a)
}

fun testRetainAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5"))
    assertFalse(a.retainAll(listOf("1", "2", "3", "4", "5")))
    assertEquals(listOf("1", "2", "3", "4", "5"), a)
    assertTrue(a.retainAll(listOf("5", "3", "1")))
    assertEquals(listOf("1", "3", "5"), a)
}

fun testEquals() {
    konst a = ArrayList(listOf("1", "2", "3"))
    assertTrue(a == listOf("1", "2", "3"))
    assertFalse(a == listOf("2", "3", "1")) // order matters
    assertFalse(a == listOf("1", "2", "4"))
    assertFalse(a == listOf("1", "2"))
}

fun testHashCode() {
    konst a = ArrayList(listOf("1", "2", "3"))
    assertTrue(a.hashCode() == listOf("1", "2", "3").hashCode())
}

fun testToString() {
    konst a = ArrayList(listOf("1", "2", "3"))
    assertTrue(a.toString() == listOf("1", "2", "3").toString())
}


fun testSubList() {
    konst a0 = ArrayList(listOf("0", "1", "2", "3", "4"))
    konst a = a0.subList(1, 4)
    assertEquals(3, a.size)
    assertEquals("1", a[0])
    assertEquals("2", a[1])
    assertEquals("3", a[2])
    assertTrue(a == listOf("1", "2", "3"))
    assertTrue(a.hashCode() == listOf("1", "2", "3").hashCode())
    assertTrue(a.toString() == listOf("1", "2", "3").toString())
}

fun testResize() {
    konst a = ArrayList<String>()
    konst n = 10000
    for (i in 1..n)
        assertTrue(a.add(i.toString()))
    assertEquals(n, a.size)
    for (i in 1..n)
        assertEquals(i.toString(), a[i - 1])
    a.trimToSize()
    assertEquals(n, a.size)
    for (i in 1..n)
        assertEquals(i.toString(), a[i - 1])
}

fun testSubListContains() {
    konst a = ArrayList(listOf("1", "2", "3", "4"))
    konst s = a.subList(1, 3)
    assertTrue(a.contains("1"))
    assertFalse(s.contains("1"))
    assertTrue(a.contains("2"))
    assertTrue(s.contains("2"))
    assertTrue(a.contains("3"))
    assertTrue(s.contains("3"))
    assertTrue(a.contains("4"))
    assertFalse(s.contains("4"))
}

fun testSubListIndexOf() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "1"))
    konst s = a.subList(1, 3)
    assertEquals(0, a.indexOf("1"))
    assertEquals(-1, s.indexOf("1"))
    assertEquals(1, a.indexOf("2"))
    assertEquals(0, s.indexOf("2"))
    assertEquals(2, a.indexOf("3"))
    assertEquals(1, s.indexOf("3"))
    assertEquals(3, a.indexOf("4"))
    assertEquals(-1, s.indexOf("4"))
}

fun testSubListLastIndexOf() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "1"))
    konst s = a.subList(1, 3)
    assertEquals(4, a.lastIndexOf("1"))
    assertEquals(-1, s.lastIndexOf("1"))
    assertEquals(1, a.lastIndexOf("2"))
    assertEquals(0, s.lastIndexOf("2"))
    assertEquals(2, a.lastIndexOf("3"))
    assertEquals(1, s.lastIndexOf("3"))
    assertEquals(3, a.lastIndexOf("4"))
    assertEquals(-1, s.lastIndexOf("4"))
}

fun testSubListClear() {
    konst a = ArrayList(listOf("1", "2", "3", "4"))
    konst s = a.subList(1, 3)
    assertEquals(listOf("2", "3"), s)

    s.clear()
    assertEquals(listOf<String>(), s)
    assertEquals(listOf("1", "4"), a)
}

fun testSubListSubListClear() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5", "6"))
    konst s = a.subList(1, 5)
    konst q = s.subList(1, 3)
    assertEquals(listOf("2", "3", "4", "5"), s)
    assertEquals(listOf("3", "4"), q)

    q.clear()
    assertEquals(listOf<String>(), q)
    assertEquals(listOf("2", "5"), s)
    assertEquals(listOf("1", "2", "5", "6"), a)
}

fun testSubListAdd() {
    konst a = ArrayList(listOf("1", "2", "3", "4"))
    konst s = a.subList(1, 3)
    assertEquals(listOf("2", "3"), s)

    assertTrue(s.add("5"))
    assertEquals(listOf("2", "3", "5"), s)
    assertEquals(listOf("1", "2", "3", "5", "4"), a)
}

fun testSubListSubListAdd() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5", "6"))
    konst s = a.subList(1, 5)
    konst q = s.subList(1, 3)
    assertEquals(listOf("2", "3", "4", "5"), s)
    assertEquals(listOf("3", "4"), q)

    assertTrue(q.add("7"))
    assertEquals(listOf("3", "4", "7"), q)
    assertEquals(listOf("2", "3", "4", "7", "5"), s)
    assertEquals(listOf("1", "2", "3", "4", "7", "5", "6"), a)
}

fun testSubListAddAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4"))
    konst s = a.subList(1, 3)
    assertEquals(listOf("2", "3"), s)

    assertTrue(s.addAll(listOf("5", "6")))
    assertEquals(listOf("2", "3", "5", "6"), s)
    assertEquals(listOf("1", "2", "3", "5", "6", "4"), a)
}

fun testSubListSubListAddAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5", "6"))
    konst s = a.subList(1, 5)
    konst q = s.subList(1, 3)
    assertEquals(listOf("2", "3", "4", "5"), s)
    assertEquals(listOf("3", "4"), q)

    assertTrue(q.addAll(listOf("7", "8")))
    assertEquals(listOf("3", "4", "7", "8"), q)
    assertEquals(listOf("2", "3", "4", "7", "8", "5"), s)
    assertEquals(listOf("1", "2", "3", "4", "7", "8", "5", "6"), a)
}

fun testSubListRemoveAt() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5"))
    konst s = a.subList(1, 4)
    assertEquals(listOf("2", "3", "4"), s)

    assertEquals("3", s.removeAt(1))
    assertEquals(listOf("2", "4"), s)
    assertEquals(listOf("1", "2", "4", "5"), a)
}

fun testSubListSubListRemoveAt() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5", "6", "7"))
    konst s = a.subList(1, 6)
    konst q = s.subList(1, 4)
    assertEquals(listOf("2", "3", "4", "5", "6"), s)
    assertEquals(listOf("3", "4", "5"), q)

    assertEquals("4", q.removeAt(1))
    assertEquals(listOf("3", "5"), q)
    assertEquals(listOf("2", "3", "5", "6"), s)
    assertEquals(listOf("1", "2", "3", "5", "6", "7"), a)
}

fun testSubListRemoveAll() {
    konst a = ArrayList(listOf("1", "2", "3", "3", "4", "5"))
    konst s = a.subList(1, 5)
    assertEquals(listOf("2", "3", "3", "4"), s)

    assertTrue(s.removeAll(listOf("3", "5")))
    assertEquals(listOf("2", "4"), s)
    assertEquals(listOf("1", "2", "4", "5"), a)
}

fun testSubListSubListRemoveAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5", "6", "7"))
    konst s = a.subList(1, 6)
    konst q = s.subList(1, 4)
    assertEquals(listOf("2", "3", "4", "5", "6"), s)
    assertEquals(listOf("3", "4", "5"), q)

    assertTrue(q.removeAll(listOf("4", "6")))
    assertEquals(listOf("3", "5"), q)
    assertEquals(listOf("2", "3", "5", "6"), s)
    assertEquals(listOf("1", "2", "3", "5", "6", "7"), a)
}

fun testSubListRetainAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5"))
    konst s = a.subList(1, 4)
    assertEquals(listOf("2", "3", "4"), s)

    assertTrue(s.retainAll(listOf("5", "3")))
    assertEquals(listOf("3"), s)
    assertEquals(listOf("1", "3", "5"), a)
}

fun testSubListSubListRetainAll() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5", "6", "7"))
    konst s = a.subList(1, 6)
    konst q = s.subList(1, 4)
    assertEquals(listOf("2", "3", "4", "5", "6"), s)
    assertEquals(listOf("3", "4", "5"), q)

    assertTrue(q.retainAll(listOf("5", "3")))
    assertEquals(listOf("3", "5"), q)
    assertEquals(listOf("2", "3", "5", "6"), s)
    assertEquals(listOf("1", "2", "3", "5", "6", "7"), a)
}

fun testIteratorRemove() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5"))
    konst it = a.iterator()
    while (it.hasNext())
        if (it.next()[0].toInt() % 2 == 0)
            it.remove()
    assertEquals(listOf("1", "3", "5"), a)
}

fun testIteratorAdd() {
    konst a = ArrayList(listOf("1", "2", "3", "4", "5"))
    konst it = a.listIterator()
    while (it.hasNext()) {
        konst next = it.next()
        if (next[0].toInt() % 2 == 0)
            it.add("-" + next)
    }
    assertEquals(listOf("1", "2", "-2", "3", "4", "-4", "5"), a)
}

@Test fun runTest() {
    testBasic()
    testIterator()
    testRemove()
    testRemoveAll()
    testRetainAll()
    testEquals()
    testHashCode()
    testToString()
    testSubList()
    testResize()
    testSubListContains()
    testSubListIndexOf()
    testSubListLastIndexOf()
    testSubListClear()
    testSubListSubListClear()
    testSubListAdd()
    testSubListSubListAdd()
    testSubListAddAll()
    testSubListSubListAddAll()
    testSubListRemoveAt()
    testSubListSubListRemoveAt()
    testSubListRemoveAll()
    testSubListSubListRemoveAll()
    testSubListSubListRemoveAll()
    testSubListRetainAll()
    testSubListSubListRetainAll()
    testIteratorRemove()
    testIteratorAdd()

    println("OK")
}