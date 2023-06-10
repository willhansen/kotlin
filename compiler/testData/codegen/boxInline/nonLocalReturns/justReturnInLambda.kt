// FILE: 1.kt

package test

public inline fun <R> doCall(block: ()-> R) : R {
    return block()
}

// FILE: 2.kt

import test.*

class Z {}

fun test1(nonLocal: String): String {

    konst localResult = doCall {
        return nonLocal
    }
}

fun box(): String {
    konst test2 = test1("OK_NONLOCAL")
    if (test2 != "OK_NONLOCAL") return "test2: ${test2}"

    return "OK"
}
