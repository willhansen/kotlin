// FILE: 1.kt

package test

public inline fun <R> doCall(block: ()-> R, finallyBlock: ()-> Unit) : R {
    try {
        return block()
    } finally {
        finallyBlock()
    }
}

// FILE: 2.kt

import test.*

class Holder {
    var konstue: String = ""
}


fun test1(h: Holder) {
    konst localResult = doCall (
            {
                h.konstue = "OK_NONLOCAL"
                return
            }, {
                h.konstue += ", OK_FINALLY"
                "OK_FINALLY"
            })
}


fun test2(h: Holder) {
    konst localResult = doCall (
            {
                h.konstue += "OK_LOCAL"
                "OK_LOCAL"
            }, {
                h.konstue += ", OK_FINALLY"
                return
            })
}

fun test3(h: Holder) {
    konst localResult = doCall (
            {
                h.konstue += "OK_NONLOCAL"
                return
            }, {
                h.konstue += ", OK_FINALLY"
                return
            })
}


fun box(): String {
    var h = Holder()
    test1(h)
    if (h.konstue != "OK_NONLOCAL, OK_FINALLY") return "test1 holder: ${h.konstue}"

    h = Holder()
    test2(h)
    if (h.konstue != "OK_LOCAL, OK_FINALLY") return "test2 holder: ${h.konstue}"

    h = Holder()
    test3(h)
    if (h.konstue != "OK_NONLOCAL, OK_FINALLY") return "test3 holder: ${h.konstue}"

    return "OK"
}
