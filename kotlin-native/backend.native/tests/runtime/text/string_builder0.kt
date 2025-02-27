/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package runtime.text.string_builder0

import kotlin.test.*

// Utils ====================================================================================================
fun assertTrue(cond: Boolean) {
    if (!cond)
        throw AssertionError("Condition expected to be true")
}

fun assertFalse(cond: Boolean) {
    if (cond)
        throw AssertionError("Condition expected to be false")
}

fun assertEquals(konstue1: String, konstue2: String) {
    if (konstue1 != konstue2)
        throw AssertionError("FAIL: '" + konstue1 + "' != '" + konstue2 + "'")
}

fun assertEquals(konstue1: Int, konstue2: Int) {
    if (konstue1 != konstue2)
        throw AssertionError("FAIL" + konstue1.toString() + " != " + konstue2.toString())
}

fun assertEquals(builder: StringBuilder, content: String) = assertEquals(builder.toString(), content)

// IndexOutOfBoundsException.
fun assertException(body: () -> Unit) {
    try {
        body()
        throw AssertionError ("Test failed: no IndexOutOfBoundsException on wrong indices")
    } catch (e: IndexOutOfBoundsException) {
    } catch (e: IllegalArgumentException) {}
}

// Insert ===================================================================================================
fun testInsertString(initial: String, index: Int, toInsert: String, expected: String) {
    assertEquals(StringBuilder(initial).insert(index, toInsert),                 expected)
    assertEquals(StringBuilder(initial).insert(index, toInsert.toCharArray()),   expected)
    assertEquals(StringBuilder(initial).insert(index, toInsert as CharSequence), expected)
}

fun testInsertStringException(initial: String, index: Int, toInsert: String) {
    assertException { StringBuilder(initial).insert(index, toInsert) }
    assertException { StringBuilder(initial).insert(index, toInsert.toCharArray()) }
    assertException { StringBuilder(initial).insert(index, toInsert as CharSequence) }
}

fun testInsertSingle(konstue: Byte) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsertSingle(konstue: Short) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsertSingle(konstue: Int) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsertSingle(konstue: Long) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsertSingle(konstue: Float) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsertSingle(konstue: Double) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsertSingle(konstue: Any?) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsertSingle(konstue: Char) {
    assertEquals(StringBuilder("abcd").insert(0, konstue),  konstue.toString() + "abcd")
    assertEquals(StringBuilder("abcd").insert(4, konstue),  "abcd" + konstue.toString())
    assertEquals(StringBuilder("abcd").insert(2, konstue),  "ab" + konstue.toString() + "cd")
    assertEquals(StringBuilder("").insert(0, konstue),      konstue.toString())
}

fun testInsert() {
    // String/CharSequence/CharArray.
    testInsertString("abcd", 0, "12", "12abcd")
    testInsertString("abcd", 4, "12", "abcd12")
    testInsertString("abcd", 2, "12", "ab12cd")
    testInsertString("", 0, "12", "12")
    testInsertStringException("a", -1, "1")
    testInsertStringException("a", 2, "1")

    // Null inserting.
    assertEquals(StringBuilder("abcd").insert(0, null as CharSequence?), "nullabcd")
    assertEquals(StringBuilder("abcd").insert(4, null as CharSequence?), "abcdnull")
    assertEquals(StringBuilder("abcd").insert(2, null as CharSequence?), "abnullcd")
    assertEquals(StringBuilder("").insert(0, null as CharSequence?), "null")

    // Subsequence of CharSequence.
    // Insert in the beginning.
    assertEquals(StringBuilder("abcd").insertRange(0, "1234", 0, 0), "abcd")                    // 0 symbols
    assertEquals(StringBuilder("abcd").insertRange(0, "1234", 0, 1), "1abcd")                   // 1 symbol
    assertEquals(StringBuilder("abcd").insertRange(0, "1234", 1, 3), "23abcd")                  // 2 symbols

    // Insert in the end.
    assertEquals(StringBuilder("abcd").insertRange(4, "1234", 0, 0), "abcd")
    assertEquals(StringBuilder("abcd").insertRange(4, "1234", 0, 1), "abcd1")
    assertEquals(StringBuilder("abcd").insertRange(4, "1234", 1, 3), "abcd23")

    // Insert in the middle.
    assertEquals(StringBuilder("abcd").insertRange(2, "1234", 0, 0), "abcd")
    assertEquals(StringBuilder("abcd").insertRange(2, "1234", 0, 1), "ab1cd")
    assertEquals(StringBuilder("abcd").insertRange(2, "1234", 1, 3), "ab23cd")

    // Incorrect indices.
    assertException { StringBuilder("a").insertRange(-1, "1", 0, 0) }
    assertException { StringBuilder("a").insertRange(2, "1", 0, 0) }
    assertException { StringBuilder("a").insertRange(1, "1", -1, 0) }
    assertException { StringBuilder("a").insertRange(1, "1", 0, 2) }
    assertException { StringBuilder("a").insertRange(1, "123", 2, 0) }

    // Other types.
    testInsertSingle(true)
    testInsertSingle(42.toByte())
    testInsertSingle(42.toShort())
    testInsertSingle(42.toInt())
    testInsertSingle(42.toLong())
    testInsertSingle(42.2.toFloat())
    testInsertSingle(42.2.toDouble())
    testInsertSingle(object {
        override fun toString(): String {
            return "Object"
        }
    })
    testInsertSingle('a')
}

// Reverse ==================================================================================================
fun testReverse(original: String, reversed: String, reversedBack: String) {
    assertEquals(StringBuilder(original).reverse(), reversed)
    assertEquals(StringBuilder(reversed).reverse(), reversedBack)
}

fun testReverse() {
    var builder = StringBuilder("123456")
    assertTrue(builder === builder.reverse())
    assertEquals(builder, "654321")

    builder.setLength(1)
    assertEquals(builder, "6")

    builder.setLength(0)
    assertEquals(builder, "")

    var str: String = "a"
    testReverse(str, str, str)

    str = "ab"
    testReverse(str, "ba", str)

    str = "abcdef"
    testReverse(str, "fedcba", str)

    str = "abcdefg"
    testReverse(str, "gfedcba", str)

    str = "\ud800\udc00"
    testReverse(str, str, str)

    str = "\udc00\ud800"
    testReverse(str, "\ud800\udc00", "\ud800\udc00")

    str = "a\ud800\udc00"
    testReverse(str, "\ud800\udc00a", str)

    str = "ab\ud800\udc00"
    testReverse(str, "\ud800\udc00ba", str)

    str = "abc\ud800\udc00"
    testReverse(str, "\ud800\udc00cba", str)

    str = "\ud800\udc00\udc01\ud801\ud802\udc02"
    testReverse(str, "\ud802\udc02\ud801\udc01\ud800\udc00",
            "\ud800\udc00\ud801\udc01\ud802\udc02")

    str = "\ud800\udc00\ud801\udc01\ud802\udc02"
    testReverse(str, "\ud802\udc02\ud801\udc01\ud800\udc00", str)

    str = "\ud800\udc00\udc01\ud801a"
    testReverse(str, "a\ud801\udc01\ud800\udc00",
            "\ud800\udc00\ud801\udc01a")

    str = "a\ud800\udc00\ud801\udc01"
    testReverse(str, "\ud801\udc01\ud800\udc00a", str)

    str = "\ud800\udc00\udc01\ud801ab"
    testReverse(str, "ba\ud801\udc01\ud800\udc00",
            "\ud800\udc00\ud801\udc01ab")

    str = "ab\ud800\udc00\ud801\udc01"
    testReverse(str, "\ud801\udc01\ud800\udc00ba", str)

    str = "\ud800\udc00\ud801\udc01"
    testReverse(str, "\ud801\udc01\ud800\udc00", str)

    str = "a\ud800\udc00z\ud801\udc01"
    testReverse(str, "\ud801\udc01z\ud800\udc00a", str)

    str = "a\ud800\udc00bz\ud801\udc01"
    testReverse(str, "\ud801\udc01zb\ud800\udc00a", str)

    str = "abc\ud802\udc02\ud801\udc01\ud800\udc00"
    testReverse(str, "\ud800\udc00\ud801\udc01\ud802\udc02cba", str)

    str = "abcd\ud802\udc02\ud801\udc01\ud800\udc00"
    testReverse(str, "\ud800\udc00\ud801\udc01\ud802\udc02dcba", str)
}

// Basic ====================================================================================================
fun testBasic() {
    konst sb = StringBuilder()
    assertEquals(0, sb.length)
    assertEquals("", sb.toString())
    sb.append(1)
    assertEquals(1, sb.length)
    assertEquals("1", sb.toString())
    sb.append(", ")
    assertEquals(3, sb.length)
    assertEquals("1, ", sb.toString())
    sb.append(true)
    assertEquals(7, sb.length)
    assertEquals("1, true", sb.toString())
    sb.append(12345678L)
    assertEquals(15, sb.length)
    assertEquals("1, true12345678", sb.toString())
    sb.append(null as CharSequence?)
    assertEquals(19, sb.length)
    assertEquals("1, true12345678null", sb.toString())

    sb.setLength(0)
    assertEquals(0, sb.length)
    assertEquals("", sb.toString())
}

@Test fun runTest() {
    testBasic()
    testInsert()
    testReverse()
    println("OK")
}