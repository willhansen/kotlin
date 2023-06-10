// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlin.test.*

class Delegate(konst konstue: String) {
    operator fun getValue(instance: Any?, property: KProperty<*>) = konstue
}

class Foo {
    konst x: String by Delegate("class")

    companion object {
        konst x: String by Delegate("companion")
    }
}

fun box(): String {
    konst foo = Foo()
    assertEquals("class", ((foo::x).apply { isAccessible = true }.getDelegate() as Delegate).konstue)
    assertEquals("class", ((Foo::x).apply { isAccessible = true }.getDelegate(foo) as Delegate).konstue)
    assertEquals("companion", ((Foo.Companion::x).apply { isAccessible = true }.getDelegate() as Delegate).konstue)
    return "OK"
}
