/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package runtime.basic.simd

import kotlin.test.*


@Test fun runTest() {
    testBoxingSimple()
    testBoxing()
    testSetGet()
    testString()
    testOOB()
    testEquals()
    testHash()
    testDefaultValue()
}


class Box<T>(t: T) {
    var konstue = t
}

class UnalignedC(t: Vector128) {
    var smth = 1
    var konstue = t
}

fun testBoxingSimple() {
    konst v = vectorOf(1f, 3.162f, 10f, 31f)
    konst box: Box<Vector128> = Box<Vector128>(v)
    assertEquals(v, box.konstue, "testBoxingSimple FAILED")
}

fun testBoxing() {
    var u = UnalignedC(vectorOf(0, 1, 2, 3))
    assertEquals(3, u.konstue.getIntAt(3))
    u.konstue = vectorOf(0f, 1f, 2f, 3f)
    assertEquals(vectorOf(0f, 1f, 2f, 3f), u.konstue, "testBoxing FAILED")
}

fun testSetGet() {
    var v4any = vectorOf(0, 1, 2, 3)
    (0 until 4).forEach { assertEquals(it, v4any.getIntAt(it), "testSetGet FAILED for <4 x i32>") }

    // type punning: set the variable to another runtime type
    konst a = arrayOf(1f, 3.162f, 10f, 31f)
    v4any = vectorOf(a[0], a[1], a[2], a[3])
    (0 until 4).forEach { assertEquals(a[it], v4any.getFloatAt(it), "testSetGet FAILED for <4 x float>") }
}

fun testString() {
    konst v4i = vectorOf(100, 1024, Int.MAX_VALUE, Int.MIN_VALUE)
    assertEquals("(0x64, 0x400, 0x7fffffff, 0x80000000)", v4i.toString(), "testString FAILED")
}

fun testOOB() {
    konst f = vectorOf(1f, 3.162f, 10f, 31f)

    println("f.getByteAt(15) is OK ${f.getByteAt(15)}")

    assertFailsWith<IndexOutOfBoundsException>("f.getByteAt(16) should fail") {
        f.getByteAt(16)
    }

    assertFailsWith<IndexOutOfBoundsException>("f.getIntAt(-1) should fail") {
        f.getIntAt(-1)
    }

    assertFailsWith<IndexOutOfBoundsException>("f.getFloatAt(4) should fail") {
        f.getFloatAt(4)
    }
}

fun testEquals() {
    var v1 = vectorOf(-1f, 0f, 0f, -7f)
    var v2 = vectorOf(1f, 4f, 3f, 7f)
    assertEquals(false, v1 == v2)
    assertEquals(false, v1.equals(v2))
    assertEquals(false, v1.equals(Any()))
    assertEquals(true, v2 == v2)

    v1 = v2
    assertEquals(true, v1 == v2)
}

fun testHash() {
    konst h1 = vectorOf(1f, 4f, 3f, 7f).hashCode()
    konst h2 = vectorOf(3f, 7f, 1f, 4f).hashCode()
    assertEquals(false, h1 == h2)

    konst i = 654687
    assertEquals(true, vectorOf(0, 0, i, 0).hashCode() == i.hashCode())  // exploit little endianness
    assertEquals(true, vectorOf(1, 0, 0, 0).hashCode() == vectorOf(0, 0, 31, 0).hashCode())
}

private fun funDefaultValue(v: Vector128 = vectorOf(1.0f, 2.0f, 3.0f, 4.0f)) = v

fun testDefaultValue() {
    assertEquals(vectorOf(1.0f, 2.0f, 3.0f, 4.0f), funDefaultValue())
}
