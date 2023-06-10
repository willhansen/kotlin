// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.test.assertEquals

konst minus0F = -0.0F
konst minus0D = -0.0

fun box(): String {
    assertEquals(-0.0F, minus0F)
    assertEquals(-0.0, minus0D)

    return "OK"
}