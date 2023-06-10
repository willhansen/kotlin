// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

public inline fun <T> T.myapply(block: T.() -> Unit): T {
    block()
    return this
}


// FILE: 2.kt
import test.*

class Test(konst konstue: () -> String) {
    fun test(): String {
        try {
            myapply {
                try {
                    return konstue()
                } catch (e: Exception) {
                } catch (e: Throwable) {
                }
            }
        } finally {

        }

        return "fail"
    }
}

fun box(): String {
    return Test { "OK" }.test()
}
