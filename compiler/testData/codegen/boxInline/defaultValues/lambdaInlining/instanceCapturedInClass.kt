// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

class A(konst konstue: String) {

    inline fun inlineFun(lambda: () -> String = { konstue }): String {
        return lambda()
    }
}

// FILE: 2.kt

import test.*

// CHECK_CONTAINS_NO_CALLS: box TARGET_BACKENDS=JS
fun box(): String {
    return A("OK").inlineFun()
}
