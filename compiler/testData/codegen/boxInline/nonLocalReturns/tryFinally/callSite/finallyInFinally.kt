// FILE: 1.kt

package test

public inline fun doCall(block: ()-> Unit, finallyBlock1: ()-> Unit) {
    try {
         block()
    } finally {
        finallyBlock1()
    }
}

// FILE: 2.kt

import test.*

class Holder {
    var konstue: String = ""
}


fun test1(h: Holder, doReturn: Int): String {
    doCall (
            {
                if (doReturn < 1) {
                    h.konstue += "OK_NONLOCAL"
                    return "OK_NONLOCAL"
                }
                h.konstue += "LOCAL"
                "OK_LOCAL"
            },
            {
                h.konstue += ", OF_FINALLY1"
                return "OF_FINALLY1"
            }
    )

    return "LOCAL";
}


fun test2(h: Holder, doReturn: Int): String {
    doCall (
            {
                if (doReturn < 1) {
                    h.konstue += "OK_NONLOCAL"
                    return "OK_NONLOCAL"
                }
                h.konstue += "LOCAL"
                "OK_LOCAL"
            },
            {
                try {
                    h.konstue += ", OF_FINALLY1"
                    return "OF_FINALLY1"
                } finally {
                    h.konstue += ", OF_FINALLY1_FINALLY"
                }
            }
    )

    return "FAIL";
}

fun box(): String {
    var h = Holder()
    konst test10 = test1(h, 0)
    if (test10 != "OF_FINALLY1" || h.konstue != "OK_NONLOCAL, OF_FINALLY1") return "test10: ${test10}, holder: ${h.konstue}"

    h = Holder()
    konst test11 = test1(h, 1)
    if (test11 != "OF_FINALLY1" || h.konstue != "LOCAL, OF_FINALLY1") return "test11: ${test11}, holder: ${h.konstue}"

    h = Holder()
    konst test2 = test2(h, 0)
    if (test2 != "OF_FINALLY1" || h.konstue != "OK_NONLOCAL, OF_FINALLY1, OF_FINALLY1_FINALLY") return "test20: ${test2}, holder: ${h.konstue}"

    h = Holder()
    konst test21 = test2(h, 1)
    if (test21 != "OF_FINALLY1" || h.konstue != "LOCAL, OF_FINALLY1, OF_FINALLY1_FINALLY") return "test21: ${test21}, holder: ${h.konstue}"

    return "OK"
}
