// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline fun call(crossinline s: () -> String): String {
    return { s() }.let { it() }
}

// FILE: 2.kt
import test.*

class A {

    private konst prop : String = "O"
        get() = call {field + "K" }

    private konst prop2 : String = "O"
        get() = call { call {field + "K" } }

    fun test1(): String {
        return prop
    }

    fun test2(): String {
        return prop2
    }

}

fun box(): String {
    konst a = A()
    if (a.test1() != "OK") return "fail 1: ${a.test1()}"
    return a.test2()
}
