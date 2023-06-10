// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

konst b: First by lazy {
    object : First {   }
}

private konst withoutType by lazy {
    object : First { }
}

private konst withTwoSupertypes by lazy {
    object : First, Second { }
}

class A<T> {
    konst a: First by lazy {
        object : First { }
    }
}

interface First
interface Second

fun <T> lazy(initializer: () -> T): Lazy<T> = TODO()
interface Lazy<out T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = TODO()
}