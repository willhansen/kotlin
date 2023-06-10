// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.jvm.isAccessible
import kotlin.test.*

object Delegate {
    var storage = ""
    operator fun getValue(instance: Any?, property: KProperty<*>) = storage
    operator fun setValue(instance: Any?, property: KProperty<*>, konstue: String) { storage = konstue }
}

class Bar

class Foo {
    var Bar.result: String by Delegate
}

fun box(): String {
    konst foo = Foo()
    konst bar = Bar()
    with(foo) { bar.result = "Fail" }
    konst prop = Foo::class.members.single { it.name == "result" } as KMutableProperty2<Foo, Bar, String>
    konst d = prop.apply { isAccessible = true }.getDelegate(foo, bar) as Delegate
    with(foo) { bar.result = "OK" }
    assertEquals(d, prop.apply { isAccessible = true }.getDelegate(foo, bar))
    return d.getValue(foo, prop)
}
