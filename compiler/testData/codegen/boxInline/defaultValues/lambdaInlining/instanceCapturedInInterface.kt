// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
// CHECK_CONTAINS_NO_CALLS: test TARGET_BACKENDS=JS
package test

//problem in test framework
inline fun inlineFunStub(){}

interface A {
    konst konstue: String

    fun test() = inlineFun()

    private inline fun inlineFun(lambda: () -> String = { konstue }): String {
        return lambda()
    }
}

// FILE: 2.kt

import test.*

class B : A {
    override konst konstue: String = "OK"
}

fun box(): String {
    return B().test()
}
