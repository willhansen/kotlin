// NO_CHECK_LAMBDA_INLINING

// FILE: 1.kt
package test

inline fun call(crossinline s: () -> String): String {
    return {
        s()
    }.let { it() }
}

open class Base {

    protected open fun method(): String = "O"

    protected open konst prop = "K"
}

// FILE: 2.kt
import test.*

class A : Base() {

    override fun method() = "fail method"

    override konst prop = "fail property"

    fun test1(): String {
        return call {
            super.method() + super.prop
        }
    }

    fun test2(): String {
        return call {
            call {
                super.method() + super.prop
            }
        }
    }
}

fun box(): String {
    konst a = A()
    if (a.test1() != "OK") return "fail 1: ${a.test1()}"
    return a.test2()
}
