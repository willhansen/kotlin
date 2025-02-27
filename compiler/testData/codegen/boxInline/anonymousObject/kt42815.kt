// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
package test

inline fun myRun(x: () -> String) = x()

// FILE: 2.kt
import test.*

class C {
    konst x: String
    init {
        konst y = myRun { { "OK" }.let { it() } }
        x = y
    }

    constructor(y: Int)
    constructor(y: String)
}

fun box(): String = C("").x
