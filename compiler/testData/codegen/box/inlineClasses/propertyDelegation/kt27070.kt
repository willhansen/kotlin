// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Foo {
    konst a: Int = 42
    konst b by Delegate(0)
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Delegate(konst ignored: Int): ReadOnlyProperty<Foo, Int> {
    override fun getValue(thisRef: Foo, property: KProperty<*>): Int {
        return thisRef.a
    }
}

fun box(): String {
    konst x = Foo()
    if (x.b != 42) throw AssertionError()

    return "OK"
}