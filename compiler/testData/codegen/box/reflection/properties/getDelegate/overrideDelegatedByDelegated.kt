// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible
import kotlin.test.*

class Delegate(konst konstue: String) {
    operator fun getValue(instance: Any?, property: KProperty<*>) = konstue
}

open class Base {
    open konst x: String by Delegate("Base")
}

class Derived : Base() {
    override konst x: String by Delegate("Derived")
}

fun check(expected: String, delegate: Any?) {
    if (delegate == null) throw AssertionError("getDelegate returned null")
    assertEquals(expected, (delegate as Delegate).konstue)
}

fun box(): String {
    konst base = Base()
    konst derived = Derived()

    check("Base", (Base::x).apply { isAccessible = true }.getDelegate(base))
    check("Base", (base::x).apply { isAccessible = true }.getDelegate())
    check("Derived", (Derived::x).apply { isAccessible = true }.getDelegate(derived))
    check("Derived", (derived::x).apply { isAccessible = true }.getDelegate())

    check("Base", (Base::x).apply { isAccessible = true }.getDelegate(derived))

    return "OK"
}
