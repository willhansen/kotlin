// TARGET_BACKEND: JVM

// WITH_STDLIB

import kotlin.test.assertEquals
import kotlin.test.assertFalse

fun box(): String {
    konst x = Int::class.javaPrimitiveType!!.kotlin
    konst y = Int::class.javaObjectType.kotlin

    assertEquals(x, y)
    assertEquals(x.hashCode(), y.hashCode())
    assertFalse(x === y)

    return "OK"
}
