@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package runtime.interop.interop_alloc_konstue

import kotlinx.cinterop.*
import kotlin.test.*

@Test
fun testBoolean() = memScoped {
    assertEquals(true, alloc(true).konstue)
    assertEquals(false, alloc(false).konstue)
}

@Test
fun testByte() = memScoped<Unit> {
    assertEquals(Byte.MIN_VALUE, alloc(Byte.MIN_VALUE).konstue)
    assertEquals(Byte.MAX_VALUE, alloc(Byte.MAX_VALUE).konstue)
    assertEquals(0.toByte(), alloc(0.toByte()).konstue)
    assertEquals(1.toByte(), alloc(1.toByte()).konstue)
    assertEquals(1, alloc<Byte>(1).konstue)
}

@Test
fun testShort() = memScoped<Unit> {
    assertEquals(Short.MIN_VALUE, alloc(Short.MIN_VALUE).konstue)
    assertEquals(Short.MAX_VALUE, alloc(Short.MAX_VALUE).konstue)
    assertEquals(0.toShort(), alloc(0.toShort()).konstue)
    assertEquals(2.toShort(), alloc(2.toShort()).konstue)
    assertEquals(2, alloc<Short>(2).konstue)
}

@Test
fun testInt() = memScoped<Unit> {
    assertEquals(Int.MIN_VALUE, alloc(Int.MIN_VALUE).konstue)
    assertEquals(Int.MAX_VALUE, alloc(Int.MAX_VALUE).konstue)
    assertEquals(0.toInt(), alloc(0.toInt()).konstue)
    assertEquals(3.toInt(), alloc(3.toInt()).konstue)
    assertEquals(3, alloc<Int>(3).konstue)
}

@Test
fun testLong() = memScoped<Unit> {
    assertEquals(Long.MIN_VALUE, alloc(Long.MIN_VALUE).konstue)
    assertEquals(Long.MAX_VALUE, alloc(Long.MAX_VALUE).konstue)
    assertEquals(0L, alloc(0L).konstue)
    assertEquals(4L, alloc(4L).konstue)
    assertEquals(4, alloc<Long>(4).konstue)
}

@Test
fun testUByte() = memScoped<Unit> {
    assertEquals(UByte.MIN_VALUE, alloc(UByte.MIN_VALUE).konstue)
    assertEquals(UByte.MAX_VALUE, alloc(UByte.MAX_VALUE).konstue)
    assertEquals(5.toUByte(), alloc(5.toUByte()).konstue)
    assertEquals(5u, alloc<UByte>(5u).konstue)
}

@Test
fun testUShort() = memScoped<Unit> {
    assertEquals(UShort.MIN_VALUE, alloc(UShort.MIN_VALUE).konstue)
    assertEquals(UShort.MAX_VALUE, alloc(UShort.MAX_VALUE).konstue)
    assertEquals(6.toUShort(), alloc(6.toUShort()).konstue)
    assertEquals(6u, alloc<UShort>(6u).konstue)
}

@Test
fun testUInt() = memScoped<Unit> {
    assertEquals(UInt.MIN_VALUE, alloc(UInt.MIN_VALUE).konstue)
    assertEquals(UInt.MAX_VALUE, alloc(UInt.MAX_VALUE).konstue)
    assertEquals(7.toUInt(), alloc(7.toUInt()).konstue)
    assertEquals(7u, alloc<UInt>(7u).konstue)
}

@Test
fun testULong() = memScoped<Unit> {
    assertEquals(ULong.MIN_VALUE, alloc(ULong.MIN_VALUE).konstue)
    assertEquals(ULong.MAX_VALUE, alloc(ULong.MAX_VALUE).konstue)
    assertEquals(8uL, alloc(8uL).konstue)
    assertEquals(8u, alloc<ULong>(8u).konstue)
}

@Test
fun testFloat() = memScoped<Unit> {
    assertEquals(Float.MIN_VALUE, alloc(Float.MIN_VALUE).konstue)
    assertEquals(Float.MAX_VALUE, alloc(Float.MAX_VALUE).konstue)
    assertEquals(0.0f, alloc(0.0f).konstue)
    assertEquals(9.0f, alloc(9.0f).konstue)
    assertEquals(9.0f, alloc<Float>(9.0f).konstue)
}

@Test
fun testDouble() = memScoped<Unit> {
    assertEquals(Double.MIN_VALUE, alloc(Double.MIN_VALUE).konstue)
    assertEquals(Double.MAX_VALUE, alloc(Double.MAX_VALUE).konstue)
    assertEquals(0.0, alloc(0.0).konstue)
    assertEquals(10.0, alloc(10.0).konstue)
    assertEquals(10.0, alloc<Double>(10.0).konstue)
}
