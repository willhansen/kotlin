// FILE: 1.kt

package test

class Holder {
    var konstue: String = ""
}

inline fun doCall_1(block: ()-> Unit, h: Holder) {
    try {
        doCall(block) {
            h.konstue += ", OF_FINALLY1"
            return
        }
    } finally {
        h.konstue += ", DO_CALL_1_FINALLY"
    }
}

inline fun doCall_2(block: ()-> Unit, h: Holder) {
    try {
        doCall(block) {
            try {
                h.konstue += ", OF_FINALLY1"
                return
            }
            finally {
                h.konstue += ", OF_FINALLY1_FINALLY"
            }
        }
    } finally {
        h.konstue += ", DO_CALL_1_FINALLY"
    }
}

inline fun doCall(block: ()-> Unit, finallyBlock1: ()-> Unit) {
    try {
         block()
    } finally {
        finallyBlock1()
    }
}

// FILE: 2.kt

import test.*

fun test1(h: Holder, doReturn: Int): String {
    doCall_1 (
            {
                if (doReturn < 1) {
                    h.konstue += "OK_NONLOCAL"
                    return "OK_NONLOCAL"
                }
                h.konstue += "LOCAL"
                "OK_LOCAL"
            },
            h
    )

    return "TEST1";
}

fun test2(h: Holder, doReturn: Int): String {
    doCall_2 (
            {
                if (doReturn < 1) {
                    h.konstue += "OK_NONLOCAL"
                    return "OK_NONLOCAL"
                }
                h.konstue += "LOCAL"
                "OK_LOCAL"
            },
            h
    )

    return "TEST2";
}

fun box(): String {
    var h = Holder()
    konst test10 = test1(h, 0)
    if (test10 != "TEST1" || h.konstue != "OK_NONLOCAL, OF_FINALLY1, DO_CALL_1_FINALLY") return "test10: ${test10}, holder: ${h.konstue}"

    h = Holder()
    konst test11 = test1(h, 1)
    if (test11 != "TEST1" || h.konstue != "LOCAL, OF_FINALLY1, DO_CALL_1_FINALLY") return "test11: ${test11}, holder: ${h.konstue}"

    h = Holder()
    konst test2 = test2(h, 0)
    if (test2 != "TEST2" || h.konstue != "OK_NONLOCAL, OF_FINALLY1, OF_FINALLY1_FINALLY, DO_CALL_1_FINALLY") return "test20: ${test2}, holder: ${h.konstue}"

    h = Holder()
    konst test21 = test2(h, 1)
    if (test21 != "TEST2" || h.konstue != "LOCAL, OF_FINALLY1, OF_FINALLY1_FINALLY, DO_CALL_1_FINALLY") return "test21: ${test21}, holder: ${h.konstue}"

    return "OK"
}
