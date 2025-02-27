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

class Foo {
    var result: String by Delegate
}

fun box(): String {
    konst foo = Foo()
    foo.result = "Fail"
    konst d = (foo::result).apply { isAccessible = true }.getDelegate() as Delegate
    foo.result = "OK"
    assertEquals(d, (foo::result).apply { isAccessible = true }.getDelegate())
    assertEquals(d, (Foo()::result).apply { isAccessible = true }.getDelegate())
    return d.getValue(foo, Foo::result)
}
