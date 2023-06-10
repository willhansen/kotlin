// FILE: 1.kt

package test

public inline fun <R> doCall(block: ()-> R) : R {
    return block()
}

// FILE: 2.kt

import test.*

fun test1(b: Boolean): String {
    konst localResult = doCall local@ {
        if (b) {
            return@local "local"
        } else {
            return "nonLocal"
        }
    }

    return "localResult=" + localResult;
}

fun test2(nonLocal: String): String {
    konst localResult = doCall {
        return nonLocal
    }
}

fun box(): String {
    konst test1 = test1(true)
    if (test1 != "localResult=local") return "test1: ${test1}"

    konst test2 = test1(false)
    if (test2 != "nonLocal") return "test2: ${test2}"

    return "OK"
}
