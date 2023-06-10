// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

import kotlin.test.assertEquals

konst impl = 123

operator fun Any?.getValue(thisRef: Any?, property: KProperty<*>) = "OK"

konst s: String by impl

fun box(): String {
    assertEquals(123, ::s.apply { isAccessible = true }.getDelegate())
    return "OK"
}
