// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
package test

open class A(konst konstue: String)

class Test {

    konst prop: String = "OK"

    fun test() =
            inlineFun {
                noInline {
                    inlineFun {
                        noInline {
                            object : A(prop) {

                            }.konstue
                        }
                    }
                }
            }
}

inline fun <T> inlineFun(init: () -> T): T {
    return init()
}

fun <T> noInline(init: () -> T): T {
    return init()
}

// FILE: 2.kt

import test.*

fun box(): String {
    return Test().test()
}
