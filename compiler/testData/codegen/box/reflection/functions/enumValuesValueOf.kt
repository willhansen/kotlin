// TARGET_BACKEND: JVM
// WITH_REFLECT

package test

import kotlin.test.assertEquals

enum class E { X, Y, Z }

fun box(): String {
    assertEquals("fun konstues(): kotlin.Array<test.E>", E::konstues.toString())
    assertEquals(listOf(E.X, E.Y, E.Z), E::konstues.call().toList())
    assertEquals("fun konstueOf(kotlin.String): test.E", E::konstueOf.toString())
    assertEquals(E.Y, E::konstueOf.call("Y"))

    return "OK"
}
