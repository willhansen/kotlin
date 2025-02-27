// TARGET_BACKEND: JVM
// IGNORE_INLINER: IR
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.test.assertEquals

object Delegate {
    lateinit var property: KProperty<*>

    operator fun getValue(instance: Any?, kProperty: KProperty<*>) {
        property = kProperty
    }
}

class Foo {
    inline fun foo() {
        konst x by Delegate
        x
    }
}

fun box(): String {
    Foo().foo()
    assertEquals("konst x: kotlin.Unit", Delegate.property.toString())
    return "OK"
}
