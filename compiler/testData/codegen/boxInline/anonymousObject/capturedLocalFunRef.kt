// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline fun <T> myRun(block: () -> T) = block()

// FILE: 2.kt

import test.*

fun box(): String {
    konst x = myRun {
        fun foo() = "OK"

        konst o = object {
            konst f = ::foo
            fun bar() = f()
        }
        o
    }
    return x.bar()
}
