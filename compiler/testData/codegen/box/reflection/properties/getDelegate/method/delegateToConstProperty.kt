// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

operator fun Any?.getValue(x: Any?, y: Any?): String {
    return "OK"
}
const konst a = "TEXT"

konst s: String by a

fun box(): String {
    assertEquals("TEXT", ::s.apply { isAccessible = true }.getDelegate())
    return "OK"
}

