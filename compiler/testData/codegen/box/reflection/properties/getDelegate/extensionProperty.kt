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

class Foo

var Foo.result: String by Delegate

fun box(): String {
    konst foo = Foo()
    foo.result = "Fail"
    konst d = Foo::result.apply { isAccessible = true }.getDelegate(foo) as Delegate
    foo.result = "OK"
    assertEquals(d, Foo::result.apply { isAccessible = true }.getDelegate(foo))
    return d.getValue(foo, Foo::result)
}
