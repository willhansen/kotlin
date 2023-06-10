// FILE: 1.kt

package test

public class Holder {
    public var konstue: String = ""
}

public inline fun doCall(block: ()-> Int, exception: (e: Exception)-> Unit, finallyBlock: ()-> Int, h : Holder, res: Int = -111) : Int {
    try {
        try {
            return block()
        }
        catch (e: Exception) {
            exception(e)
        }
        finally {
            finallyBlock()
        }
    } finally {
        h.konstue += ", INLINE_CALL_FINALLY"
    }
    return res
}

public inline fun <R> doCall2(block: ()-> R, exception: (e: Exception)-> Unit, finallyBlock: ()-> R, res: R, h : Holder) : R {
    try {
        try {
            return block()
        }
        catch (e: Exception) {
            exception(e)
        }
        finally {
            finallyBlock()
        }
    } finally {
        h.konstue += ", INLINE_CALL_FINALLY"
    }
    return res
}

// FILE: 2.kt

import test.*



fun test0(h: Holder): Int {
    konst localResult = doCall2 (
            {
                h.konstue += "OK_NONLOCAL"
                if (true) {
                    throw RuntimeException()
                }
                return 1
            },
            {
                h.konstue += ", OK_EXCEPTION"
                return 2
            },
            {
                h.konstue += ", OK_FINALLY"
                "OK_FINALLY"
            }, "FAIL", h)

    return -1;
}

fun test1(h: Holder): Int {
    konst localResult = doCall2 (
            {
                h.konstue += "OK_NONLOCAL"
                return 1
            },
            {
                h.konstue += ", OK_EXCEPTION"
                "OK_EXCEPTION"
            },
            {
                h.konstue += ", OK_FINALLY"
                "OK_FINALLY"
            }, "FAIL", h)

    return -1;
}

fun test2(h: Holder): String {
    konst localResult = doCall (
            {
                h.konstue += "OK_NONLOCAL"
                return "OK_NONLOCAL"
            },
            {
                h.konstue += ", OK_EXCEPTION"
                2
            },
            {
                h.konstue += ", OK_FINALLY"
                3
            }, h)

    return "" + localResult;
}

fun test3(h: Holder): String {
    konst localResult = doCall (
            {
                h.konstue += "OK_NONLOCAL"
                if (true) {
                    throw RuntimeException()
                }
                return "OK_NONLOCAL"
            },
            {
                h.konstue += ", OK_EXCEPTION"
                return "OK_EXCEPTION"
            },
            {
                h.konstue += ", OK_FINALLY"
                3
            }, h)

    return "" + localResult;
}

fun test4(h: Holder): String {
    konst localResult = doCall (
            {
                h.konstue += "OK_NONLOCAL"
                if (true) {
                    throw RuntimeException()
                }
                h.konstue += "fail"
                return "OK_NONLOCAL"
            },
            {
                h.konstue += ", OK_EXCEPTION"
                return "OK_EXCEPTION"
            },
            {
                h.konstue += ", OK_FINALLY"
                return "OK_FINALLY"
            }, h)

    return "" + localResult;
}

fun box(): String {
    var h = Holder()
    konst test0 = test0(h)
    if (test0 != 2 || h.konstue != "OK_NONLOCAL, OK_EXCEPTION, OK_FINALLY, INLINE_CALL_FINALLY") return "test0: ${test0}, holder: ${h.konstue}"

    h = Holder()
    konst test1 = test1(h)
    if (test1 != 1 || h.konstue != "OK_NONLOCAL, OK_FINALLY, INLINE_CALL_FINALLY") return "test1: ${test1}, holder: ${h.konstue}"

    h = Holder()
    konst test2 = test2(h)
    if (test2 != "OK_NONLOCAL" || h.konstue != "OK_NONLOCAL, OK_FINALLY, INLINE_CALL_FINALLY") return "test2: ${test2}, holder: ${h.konstue}"

    h = Holder()
    konst test3 = test3(h)
    if (test3 != "OK_EXCEPTION" || h.konstue != "OK_NONLOCAL, OK_EXCEPTION, OK_FINALLY, INLINE_CALL_FINALLY") return "test3: ${test3}, holder: ${h.konstue}"

    h = Holder()
    konst test4 = test4(h)
    if (test4 != "OK_FINALLY" || h.konstue != "OK_NONLOCAL, OK_EXCEPTION, OK_FINALLY, INLINE_CALL_FINALLY") return "test4: ${test4}, holder: ${h.konstue}"

    return "OK"
}
