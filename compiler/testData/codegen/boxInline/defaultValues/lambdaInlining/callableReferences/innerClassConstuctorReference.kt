// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

class A(konst konstue: String) {

    inner class Inner {
        fun ok() = konstue
    }
}

inline fun inlineFun(a: A, lambda: () -> A.Inner = a::Inner): A.Inner {
    return lambda()
}

// FILE: 2.kt

import test.*

fun box(): String {
    return inlineFun(A("OK")).ok()
}
