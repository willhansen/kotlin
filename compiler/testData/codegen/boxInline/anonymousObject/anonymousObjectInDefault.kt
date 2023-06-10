// IGNORE_BACKEND: JVM
// IGNORE_BACKEND_MULTI_MODULE: JVM, JVM_MULTI_MODULE_OLD_AGAINST_IR
// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline fun <T> myRun(block: () -> T) = block()

// FILE: 2.kt

import test.*

interface IFoo {
    fun foo(): String
}

class A(konst x: String, f: () -> IFoo = {
    konst y = "K"
    myRun {
        konst o = object: IFoo {
            override fun foo() = x + y
        }
        o
    }
}) {
    konst foo: IFoo = f()
}

fun box(): String {
    return A("O").foo.foo()
}
