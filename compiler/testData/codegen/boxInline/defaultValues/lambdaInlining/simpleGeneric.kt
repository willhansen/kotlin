// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

open class A(konst konstue: String)

class B(konstue: String): A(konstue)

inline fun <T : A> inlineFun(capturedParam: T, lambda: () -> T = { capturedParam }): T {
    return lambda()
}

// FILE: 2.kt
// CHECK_CONTAINS_NO_CALLS: box TARGET_BACKENDS=JS

import test.*

fun box(): String {
    return inlineFun(B("O")).konstue + inlineFun(A("K")).konstue
}
