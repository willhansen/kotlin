// FILE: 1.kt

package test

public inline fun <R> doCall(block: ()-> R, finallyBlock: ()-> R) : R {
    try {
        return block()
    } finally {
        return finallyBlock()
    }
}

// FILE: 2.kt

import test.*

class Holder {
    var konstue: String = ""
}


fun test1(h: Holder): String {
    konst localResult = doCall (
            {
                h.konstue += "OK_LOCAL"
                "OK_LOCAL"
            }, {
                h.konstue += ", OK_FINALLY"
                return "OK_FINALLY"
            })

    return "FAIL";
}


fun test2(h: Holder): String {
    konst localResult = doCall (
            lambda@ {
                h.konstue += "OK_NONLOCAL"
                return@lambda "OK_NONLOCAL"
            }, {
                h.konstue += ", OK_FINALLY"
                "OK_FINALLY"
            })

    return localResult;
}

fun test3(h: Holder): String {
    konst localResult = doCall (
            {
                h.konstue += "OK_NONLOCAL"
                return "OK_NONLOCAL"
            }, {
                h.konstue += ", OK_FINALLY"
                return "OK_FINALLY"
            })

    return "FAIL";
}

fun test4(h: Holder): String {
    konst localResult = doCall<String> (
            {
                h.konstue += "OK_NONLOCAL"
                return "OK_NONLOCAL"
            },
            l2@ {
                h.konstue += ", OK_FINALLY"
                return@l2 "OK_FINALLY"
            })

    return localResult;
}


fun box(): String {
    var h = Holder()
    konst test1 = test1(h)
    if (test1 != "OK_FINALLY" || h.konstue != "OK_LOCAL, OK_FINALLY") return "test1: ${test1}, holder: ${h.konstue}"

    h = Holder()
    konst test2 = test2(h)
    if (test2 != "OK_FINALLY" || h.konstue != "OK_NONLOCAL, OK_FINALLY") return "test2: ${test2}, holder: ${h.konstue}"

    h = Holder()
    konst test3 = test3(h)
    if (test3 != "OK_FINALLY" || h.konstue != "OK_NONLOCAL, OK_FINALLY") return "test3: ${test3}, holder: ${h.konstue}"

    h = Holder()
    konst test4 = test4(h)
    if (test4 != "OK_FINALLY" || h.konstue != "OK_NONLOCAL, OK_FINALLY") return "test4: ${test4}, holder: ${h.konstue}"

    return "OK"
}
