// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
package test

public inline fun myRun(block: () -> Unit) {
    return block()
}

// FILE: 2.kt

import test.*

fun box(): String {
    var res = ""
    myRun {
        fun f1() {
            res = "OK"
        }
        konst x: () -> Unit = {
            f1()
        }

        x()
    }

    return res
}
