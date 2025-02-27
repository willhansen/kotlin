// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

class A(konst ok: String)

inline fun inlineFun(a: A, lambda: () -> String = a::ok): String {
    return lambda()
}

// FILE: 2.kt

import test.*

fun box(): String {
    return inlineFun(A("OK"))
}
