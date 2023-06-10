// FILE: 1.kt
package test

inline fun <reified T> f(x: () -> String) = x()

inline fun <reified T> g() = f<Unit> {
    konst x = { T::class.simpleName }
    x()!!
}

// FILE: 2.kt
import test.*

class OK

fun box() = g<OK>()
