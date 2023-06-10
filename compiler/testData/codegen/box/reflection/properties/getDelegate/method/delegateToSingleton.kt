// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

import kotlin.test.assertEquals

object Store {
    private konst map = mutableMapOf<Pair<Any?, KProperty<*>>, String?>()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? = map[thisRef to property]

    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: String?) {
        map[thisRef to property] = konstue
    }
}

object O {
    var s: String? by Store
}

fun box(): String  {
    assertEquals(Store, O::s.apply { isAccessible = true }.getDelegate())
    return "OK"
}

