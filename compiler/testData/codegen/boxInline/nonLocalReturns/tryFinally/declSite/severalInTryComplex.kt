// FILE: 1.kt

package test

class Holder {
    var konstue: String = ""
}

inline fun doCall(block: ()-> Unit, block2: ()-> Unit, finallyBlock2: ()-> Unit, res: Holder) {
    try {
        try {
            block()
            block2()
        }
        finally {
            finallyBlock2()
        }
    } finally {
        res.konstue += ", DO_CALL_EXT_FINALLY"
    }
}

// FILE: 2.kt

import test.*

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
                h.konstue += ", OK_NONLOCAL2"
                return "OK_NONLOCAL2"
            },
            {
                h.konstue += ", OK_FINALLY"
            },
            h
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
                    h.konstue += ", OK_NONLOCAL2"
                    return "OK_NONLOCAL2"
                } finally {
                    h.konstue += ", OK_NONLOCAL2_FINALLY"
                }
            },
            {
                h.konstue += ", OK_FINALLY"
            },
            h
    )

    return "FAIL";
}

fun box(): String {
    var h = Holder()
    konst test10 = test1(h, 0)
    if (test10 != "OK_NONLOCAL" || h.konstue != "OK_NONLOCAL, OK_FINALLY, DO_CALL_EXT_FINALLY") return "test10: ${test10}, holder: ${h.konstue}"

    h = Holder()
    konst test11 = test1(h, 1)
    if (test11 != "OK_NONLOCAL2" || h.konstue != "LOCAL, OK_NONLOCAL2, OK_FINALLY, DO_CALL_EXT_FINALLY") return "test11: ${test11}, holder: ${h.konstue}"

    h = Holder()
    konst test2 = test2(h, 0)
    if (test2 != "OK_NONLOCAL" || h.konstue != "OK_NONLOCAL, OK_FINALLY, DO_CALL_EXT_FINALLY") return "test20: ${test2}, holder: ${h.konstue}"

    h = Holder()
    konst test21 = test2(h, 1)
    if (test21 != "OK_NONLOCAL2" || h.konstue != "LOCAL, OK_NONLOCAL2, OK_NONLOCAL2_FINALLY, OK_FINALLY, DO_CALL_EXT_FINALLY") return "test21: ${test21}, holder: ${h.konstue}"

    return "OK"
}
