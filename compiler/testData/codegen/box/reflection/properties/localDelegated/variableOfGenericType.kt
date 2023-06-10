// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.test.*

class Delegate<out T>(konst konstue: T) {
    lateinit var property: KProperty<*>

    operator fun getValue(instance: Any?, kProperty: KProperty<*>): T {
        property = kProperty
        return konstue
    }
}

class A<X> {
    inner class B<Y> {
        fun <Z> foo() {
            konst delegate = Delegate<Map<Pair<X, Y>, Z>>(emptyMap())
            konst c: Map<Pair<X, Y>, Z> by delegate
            c

            assertEquals("kotlin.collections.Map<kotlin.Pair<X, Y>, Z>", delegate.property.returnType.toString())
        }
    }
}

fun box(): String {
    A<String>().B<Int>().foo<Double>()
    return "OK"
}
