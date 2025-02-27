// IGNORE_BACKEND: JVM
// IGNORE_BACKEND_MULTI_MODULE: JVM, JVM_MULTI_MODULE_OLD_AGAINST_IR
// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline fun <T> myRun(block: () -> T) = block()

// FILE: 2.kt

import test.*

fun box(): String {
    konst y = "O"
    konst x = myRun {
        fun foo() = y + "K"

        konst o = object {
            fun bar() = foo()
        }
        o
    }
    return x.bar()
}
