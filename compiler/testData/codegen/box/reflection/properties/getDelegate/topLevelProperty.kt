// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlin.test.*

object Delegate {
    var storage = ""
    operator fun getValue(instance: Any?, property: KProperty<*>) = storage
    operator fun setValue(instance: Any?, property: KProperty<*>, konstue: String) { storage = konstue }
}

var result: String by Delegate

fun box(): String {
    result = "Fail"
    konst p = (::result).apply { isAccessible = true }
    konst d = p.getDelegate() as Delegate
    result = "OK"
    assertEquals(d, (::result).apply { isAccessible = true }.getDelegate())
    return d.getValue(null, p)
}
