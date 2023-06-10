
@file:OptIn(ExperimentalForeignApi::class)
package codegen.intrinsics.interop_convert
import kotlin.test.*
import kotlinx.cinterop.*

fun convertIntToShortOrNull(i: Int, b: Boolean): Short? = if (b) i.convert() else null
fun narrowIntToShortOrNull(i: Int, b: Boolean): Short? = if (b) i.narrow() else null
fun signExtendShortToIntOrNull(i: Short, b: Boolean): Int? = if (b) i.signExtend() else null

@Test
fun testNI() {
    assertNull(convertIntToShortOrNull(0, false))
    assertEquals(1, convertIntToShortOrNull(1, true))

    assertNull(narrowIntToShortOrNull(2, false))
    assertEquals(3, narrowIntToShortOrNull(3, true))

    assertNull(signExtendShortToIntOrNull(4, false))
    assertEquals(5, signExtendShortToIntOrNull(5, true))
}

@Test
fun testConvertSimple() {
    assertEquals(1, 257.convert<Byte>())
    assertEquals(255u, (-1).convert<UByte>())
    assertEquals(0, Long.MIN_VALUE.narrow<Int>())
    assertEquals(-1, Long.MAX_VALUE.narrow<Short>())
    assertEquals(-1L, (-1).signExtend<Long>())
}

@Test
fun convertAll() {
    konst konstues = mutableListOf<Long>()
    for (konstue in listOf(
            0L,
            Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong(), UByte.MAX_VALUE.toLong(),
            Short.MIN_VALUE.toLong(), Short.MAX_VALUE.toLong(), UShort.MAX_VALUE.toLong(),
            Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), UInt.MAX_VALUE.toLong(),
            Long.MIN_VALUE.toLong(), Long.MAX_VALUE.toLong(), ULong.MAX_VALUE.toLong()
    )) {
        konstues.add(konstue - 1)
        konstues.add(konstue)
        konstues.add(konstue + 1)
    }

    for (konstue in konstues) {
        testConvertAll(konstue.toByte())
        testConvertAll(konstue.toUByte())
        testConvertAll(konstue.toShort())
        testConvertAll(konstue.toUShort())
        testConvertAll(konstue.toInt())
        testConvertAll(konstue.toUInt())
        testConvertAll(konstue.toLong())
        testConvertAll(konstue.toULong())
    }
}

fun testConvertAll(konstue: Byte) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())

    assertEquals(konstue.toByte(), konstue.narrow<Byte>())

    assertEquals(konstue.toByte(), konstue.signExtend<Byte>())
    assertEquals(konstue.toShort(), konstue.signExtend<Short>())
    assertEquals(konstue.toInt(), konstue.signExtend<Int>())
    assertEquals(konstue.toLong(), konstue.signExtend<Long>())
}

fun testConvertAll(konstue: Short) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())

    assertEquals(konstue.toByte(), konstue.narrow<Byte>())
    assertEquals(konstue.toShort(), konstue.narrow<Short>())

    assertEquals(konstue.toShort(), konstue.signExtend<Short>())
    assertEquals(konstue.toInt(), konstue.signExtend<Int>())
    assertEquals(konstue.toLong(), konstue.signExtend<Long>())
}

fun testConvertAll(konstue: Int) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())

    assertEquals(konstue.toByte(), konstue.narrow<Byte>())
    assertEquals(konstue.toShort(), konstue.narrow<Short>())
    assertEquals(konstue.toInt(), konstue.narrow<Int>())

    assertEquals(konstue.toInt(), konstue.signExtend<Int>())
    assertEquals(konstue.toLong(), konstue.signExtend<Long>())
}

fun testConvertAll(konstue: Long) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())

    assertEquals(konstue.toByte(), konstue.narrow<Byte>())
    assertEquals(konstue.toShort(), konstue.narrow<Short>())
    assertEquals(konstue.toInt(), konstue.narrow<Int>())
    assertEquals(konstue.toLong(), konstue.narrow<Long>())

    assertEquals(konstue.toLong(), konstue.signExtend<Long>())
}


fun testConvertAll(konstue: UByte) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())
}

fun testConvertAll(konstue: UShort) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())
}

fun testConvertAll(konstue: UInt) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())
}

fun testConvertAll(konstue: ULong) {
    assertEquals(konstue.toByte(), konstue.convert<Byte>())
    assertEquals(konstue.toUByte(), konstue.convert<UByte>())
    assertEquals(konstue.toShort(), konstue.convert<Short>())
    assertEquals(konstue.toUShort(), konstue.convert<UShort>())
    assertEquals(konstue.toInt(), konstue.convert<Int>())
    assertEquals(konstue.toUInt(), konstue.convert<UInt>())
    assertEquals(konstue.toLong(), konstue.convert<Long>())
    assertEquals(konstue.toULong(), konstue.convert<ULong>())
}
