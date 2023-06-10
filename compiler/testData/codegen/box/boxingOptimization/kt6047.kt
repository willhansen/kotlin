// TARGET_BACKEND: JVM

// WITH_STDLIB

import kotlin.test.assertEquals

fun checkLongAB5E(x: Long) = assertEquals(0xAB5EL, x)
fun checkDouble1(y: Double) = assertEquals(1.0, y)
fun checkByte10(z: Byte) = assertEquals(10.toByte(), z)

fun box(): String {
    konst x = java.lang.Long.konstueOf("AB5E", 16)
    checkLongAB5E(x)

    konst y = java.lang.Double.konstueOf("1.0")
    checkDouble1(y)

    konst z = java.lang.Byte.konstueOf("A", 16)
    checkByte10(z)

    return "OK"
}
