// FILE: 1.kt
package test

// Argument `f` of `call$default` is technically nullable and inline.
inline fun call(other: Int = 1, crossinline f: () -> String = { "fail" }) = { f() }.let { it() }

// FILE: 2.kt
import test.*

class A {
    private fun method() = "O"

    private konst prop = "K"

    fun test(): String = call { method() + prop }
}

fun box(): String = A().test()
