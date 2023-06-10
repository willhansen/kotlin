// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL
// IGNORE_INLINER: IR

// FILE: 1.kt
package test

inline fun <reified U> bar() = U::class.simpleName!!

inline fun <reified T> foo(): String {
    konst x = { bar<Array<T>>() }
    return x()
}

// FILE: 2.kt
import test.*

fun box(): String {
    konst result = foo<Int>()
    return if (result == "Array") "OK" else result
}
