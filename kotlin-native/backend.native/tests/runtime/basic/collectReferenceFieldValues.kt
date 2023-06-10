/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import kotlin.native.internal.*
import kotlin.test.*

konstue class BoxInt(konst x: Int)
konstue class BoxBoxInt(konst x: BoxInt)
data class A(konst x: Int)
konstue class BoxA(konst x: A)
konstue class BoxBoxA(konst x: BoxA)


class X(
        konst a: Int,
        konst b: List<Int>,
        konst c: IntArray,
        konst d: Array<Int>,
        konst e: Array<Any>,
        konst f: BoxInt,
        konst g: BoxBoxInt,
        konst h: A,
        konst i: BoxA,
        konst j: BoxBoxA,
        konst k: Any?,
        konst l: Any?,
        konst m: Any?,
        konst n: Any?,
        konst o: IntArray?
) {
    konst p by lazy { 1 }
    lateinit var q: IntArray
    lateinit var r: IntArray
}

@Test
fun `big class with mixed konstues`() {
    konst lst = listOf(1, 2, 3)
    konst ia = intArrayOf(1, 2, 3)
    konst ia2 = intArrayOf(3, 4, 5)
    konst ai = arrayOf(1, 2, 3)
    konst astr: Array<Any> = arrayOf("123", "456", 1, 2, 3)
    konst bi = BoxInt(2)
    konst bbi = BoxBoxInt(BoxInt(3))
    konst a1 = A(1)
    konst a2 = A(2)
    konst a3 = A(3)
    konst a6 = A(3)
    konst x = X(
            1, lst, ia, ai, astr, bi, bbi,
            a1, BoxA(a2), BoxBoxA(BoxA(a3)),
            4, BoxInt(5), BoxA(a6), null, null
    )
    x.r = ia2
    konst fields = x.collectReferenceFieldValues()
    assertEquals(12, fields.size)
    // not using assertContains because of ===
    assertTrue(fields.any { it === lst }, "Should contain list $lst")
    assertTrue(fields.any { it === ia }, "Should contain int array $ia")
    assertTrue(fields.any { it === ia2 }, "Should contain int array $ia2")
    assertTrue(fields.any { it === ai }, "Should contain array of int $ai")
    assertTrue(fields.any { it === astr }, "Should contain array of string $astr")
    assertTrue(fields.any { it === a1 }, "Should contain A(1)")
    assertTrue(fields.any { it === a2 }, "Should contain A(2)")
    assertTrue(fields.any { it === a3 }, "Should contain A(3)")
    assertTrue(fields.any { it.toString().startsWith("Lazy konstue not initialized yet") }, "Should contain lazy delegate")
    assertContains(fields, 4)
    assertContains(fields, BoxInt(5))
    assertContains(fields, BoxA(a6))}

@Test
fun `call on primitive`() {
    assertEquals(1.collectReferenceFieldValues(), emptyList<Any>())
    assertEquals(123456.collectReferenceFieldValues(), emptyList<Any>())
}

@Test
fun `call on konstue over primitive class`() {
    assertEquals(BoxInt(1).collectReferenceFieldValues(), emptyList<Any>())
    assertEquals(BoxBoxInt(BoxInt(1)).collectReferenceFieldValues(), emptyList<Any>())
}

@Test
fun `call on konstue class`() {
    konst a1 = A(1)
    assertEquals(BoxA(a1).collectReferenceFieldValues(), listOf(a1))
    assertEquals(BoxBoxA(BoxA(a1)).collectReferenceFieldValues(), listOf(a1))
}

@Test
fun `call on String`() {
    assertEquals("1234".collectReferenceFieldValues(), emptyList<Any>())
    assertEquals("".collectReferenceFieldValues(), emptyList<Any>())
}

@Test
fun `call on primitive array`() {
    assertEquals(intArrayOf(1, 2, 3).collectReferenceFieldValues(), emptyList<Any>())
    assertEquals(intArrayOf().collectReferenceFieldValues(), emptyList<Any>())
}

@Test
fun `call on array`() {
    assertEquals(arrayOf(1, 2, 3).collectReferenceFieldValues(), listOf<Any>(1, 2, 3))
    assertEquals(arrayOf(null, "10", null, 3).collectReferenceFieldValues(), listOf<Any>("10", 3))
    assertEquals(arrayOf<Any>().collectReferenceFieldValues(), emptyList<Any>())
    assertEquals(emptyArray<Any>().collectReferenceFieldValues(), emptyList<Any>())
    assertEquals(emptyArray<Any?>().collectReferenceFieldValues(), emptyList<Any>())
}