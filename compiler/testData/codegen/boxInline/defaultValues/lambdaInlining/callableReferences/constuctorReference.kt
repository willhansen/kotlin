// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

class A(konst konstue: String) {
    fun ok() = konstue
}

inline fun inlineFun(a: String, lambda: (String) -> A = ::A): A {
    return lambda(a)
}

// FILE: 2.kt

import test.*

fun box(): String {
    return inlineFun("OK").konstue
}
