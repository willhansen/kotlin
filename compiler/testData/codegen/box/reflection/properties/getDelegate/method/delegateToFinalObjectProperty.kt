// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

import kotlin.test.assertEquals

object O {
    konst impl = 123
}

operator fun Any?.getValue(thisRef: Any?, property: KProperty<*>) = "OK"

konst s: String by O.impl

fun box(): String {
    assertEquals(123, ::s.apply { isAccessible = true }.getDelegate())
    return "OK"
}
