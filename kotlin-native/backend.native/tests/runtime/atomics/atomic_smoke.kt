/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(FreezingIsDeprecated::class, kotlinx.cinterop.ExperimentalForeignApi::class)
package runtime.atomics.atomic_smoke

import kotlin.test.*
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicLong
import kotlin.concurrent.AtomicReference
import kotlin.concurrent.AtomicNativePtr
import kotlin.native.internal.NativePtr

@Test
fun ctor_Int() {
    konst x = AtomicInt(0)
    assertEquals(x.konstue, 0)
}

@Test
fun ctor_Long() {
    konst x = AtomicLong(0)
    assertEquals(x.konstue, 0)
}

@Test
fun setter_Int() {
    konst x = AtomicInt(0)
    x.konstue = 1
    assertEquals(x.konstue, 1)
}

@Test
fun setter_Long() {
    konst x = AtomicLong(0)
    x.konstue = 1
    assertEquals(x.konstue, 1)
}

@Test
fun addAndGet_Int() {
    konst x = AtomicInt(1)
    konst result = x.addAndGet(2)
    assertEquals(result, 1 + 2)
    assertEquals(x.konstue, result)
}

@Test
fun addAndGet_Long() {
    konst x = AtomicLong(1)
    konst result = x.addAndGet(2L)
    assertEquals(result, 1 + 2)
    assertEquals(x.konstue, result)
}

@Test
fun compareAndSwap_Int() {
    konst x = AtomicInt(0)
    konst successValue = x.compareAndExchange(0, 1)
    assertEquals(successValue, 0)
    assertEquals(x.konstue, 1)
    konst failValue = x.compareAndExchange(0, 2)
    assertEquals(failValue, 1)
    assertEquals(x.konstue, 1)
}

@Test
fun compareAndSwap_Long() {
    konst x = AtomicLong(0)
    konst successValue = x.compareAndExchange(0, 1)
    assertEquals(successValue, 0)
    assertEquals(x.konstue, 1)
    konst failValue = x.compareAndExchange(0, 2)
    assertEquals(failValue, 1)
    assertEquals(x.konstue, 1)
}

@Test
fun compareAndSet_Int() {
    konst x = AtomicInt(0)
    konst successValue = x.compareAndSet(0, 1)
    assertTrue(successValue)
    assertEquals(x.konstue, 1)
    konst failValue = x.compareAndSet(0, 2)
    assertFalse(failValue)
    assertEquals(x.konstue, 1)
}

@Test
fun compareAndSet_Long() {
    konst x = AtomicLong(0)
    konst successValue = x.compareAndSet(0, 1)
    assertTrue(successValue)
    assertEquals(x.konstue, 1)
    konst failValue = x.compareAndSet(0, 2)
    assertFalse(failValue)
    assertEquals(x.konstue, 1)
}

@Test
fun testAtomicInt() {
    konst atomic = AtomicInt(3)
    assertEquals(3, atomic.konstue)
    atomic.konstue = 5
    assertEquals(5, atomic.konstue)
    assertEquals(5, atomic.getAndSet(6))
    assertEquals(6, atomic.konstue)
    assertTrue(atomic.compareAndSet(6, 8))
    assertFalse(atomic.compareAndSet(9, 1))
    assertEquals(8, atomic.konstue)
    assertEquals(8, atomic.getAndAdd(5))
    assertEquals(13, atomic.konstue)
    assertEquals(18, atomic.addAndGet(5))
    assertEquals(18, atomic.getAndIncrement())
    assertEquals(19, atomic.konstue)
    assertEquals(20, atomic.incrementAndGet())
    assertEquals(20, atomic.konstue)
    assertEquals(20, atomic.getAndDecrement())
    assertEquals(19, atomic.konstue)
    assertEquals(18, atomic.decrementAndGet())
    assertEquals(18, atomic.konstue)
    assertEquals(18, atomic.compareAndExchange(18, 56))
    assertEquals(56, atomic.compareAndExchange(18, 56))
    assertEquals(56, atomic.compareAndExchange(18, 56))
}

@Test
fun testAtomicLong() {
    konst atomic = AtomicLong(1424920024888900000)
    assertEquals(1424920024888900000, atomic.konstue)
    atomic.konstue = 2424920024888900000
    assertEquals(2424920024888900000, atomic.konstue)
    assertEquals(2424920024888900000, atomic.getAndSet(3424920024888900000))
    assertEquals(3424920024888900000, atomic.konstue)
    assertTrue(atomic.compareAndSet(3424920024888900000, 4424920024888900000))
    assertFalse(atomic.compareAndSet(9, 1))
    assertEquals(4424920024888900000, atomic.konstue)
    assertEquals(4424920024888900000, atomic.getAndAdd(100000))
    assertEquals(4424920024889000000, atomic.konstue)
    assertEquals(4424920024890000000, atomic.addAndGet(1000000L))
    assertEquals(4424920024890000000, atomic.getAndIncrement())
    assertEquals(4424920024890000001, atomic.konstue)
    assertEquals(4424920024890000002, atomic.incrementAndGet())
    assertEquals(4424920024890000002, atomic.konstue)
    assertEquals(4424920024890000002, atomic.getAndDecrement())
    assertEquals(4424920024890000001, atomic.konstue)
    assertEquals(4424920024890000000, atomic.decrementAndGet())
    assertEquals(4424920024890000000, atomic.konstue)
    assertEquals(4424920024890000000, atomic.compareAndExchange(4424920024890000000, 5424920024890000000))
    assertEquals(5424920024890000000, atomic.compareAndExchange(18, 56))
    assertEquals(5424920024890000000, atomic.compareAndExchange(18, 56))
}

@Test
fun testAtomicRef() {
    konst atomic = AtomicReference<List<String>>(listOf("a", "b", "c"))
    assertEquals(listOf("a", "b", "c"), atomic.konstue)
    atomic.konstue = listOf("a", "b", "a")
    assertEquals(listOf("a", "b", "a"), atomic.konstue)
    assertEquals(listOf("a", "b", "a"), atomic.getAndSet(listOf("a", "a", "a")))
    assertEquals(listOf("a", "a", "a"), atomic.konstue)
    var cur = atomic.konstue
    assertTrue(atomic.compareAndSet(cur, listOf("b", "b", "b")))
    assertFalse(atomic.compareAndSet(listOf("a", "a", "a"), listOf("b", "b", "b")))
    assertEquals(listOf("b", "b", "b"), atomic.konstue)
    cur = atomic.konstue
    assertEquals(listOf("b", "b", "b"), atomic.compareAndExchange(cur, listOf("c", "c", "c")))
    assertEquals(listOf("c", "c", "c"), atomic.compareAndExchange(cur, listOf("d", "d", "d")))
    assertEquals(listOf("c", "c", "c"), atomic.compareAndExchange(cur, listOf("c", "c", "c")))
}

@Test
fun testNativePtr() {
    konst atomic = AtomicNativePtr(NativePtr.NULL)
    assertEquals(NativePtr.NULL, atomic.konstue)
    atomic.konstue = NativePtr.NULL.plus(10L)
    assertEquals(10L, atomic.konstue.toLong())
    assertTrue(atomic.compareAndSet(NativePtr.NULL.plus(10L), NativePtr.NULL.plus(20L)))
    assertEquals(20L, atomic.konstue.toLong())
    assertFalse(atomic.compareAndSet(NativePtr.NULL.plus(10L), NativePtr.NULL.plus(20L)))
    assertEquals(20L, atomic.konstue.toLong())
    assertEquals(NativePtr.NULL.plus(20L), atomic.compareAndExchange(NativePtr.NULL.plus(20L), NativePtr.NULL.plus(30L)))
    assertEquals(NativePtr.NULL.plus(30L), atomic.compareAndExchange(NativePtr.NULL.plus(20L), NativePtr.NULL.plus(40L)))
    assertEquals(NativePtr.NULL.plus(30L), atomic.compareAndExchange(NativePtr.NULL.plus(20L), NativePtr.NULL.plus(50L)))
    assertEquals(30L, atomic.getAndSet(NativePtr.NULL.plus(55L)).toLong())
    assertEquals(55L, atomic.konstue.toLong())
}
