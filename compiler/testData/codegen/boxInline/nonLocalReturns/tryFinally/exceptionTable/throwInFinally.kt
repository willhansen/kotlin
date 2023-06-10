// FILE: 1.kt

package test

public inline fun <R> doCall(block: ()-> R, exception: (e: Exception)-> Unit, finallyBlock: ()-> R) : R {
    try {
        return block()
    } catch (e: Exception) {
        exception(e)
    } finally {
        return finallyBlock()
    }
}

public inline fun <R> doCall2(block: ()-> R, exception: (e: Exception)-> Unit, finallyBlock: ()-> R) : R {
    try {
        return block()
    } catch (e: Exception) {
        exception(e)
    } finally {
        finallyBlock()
    }
    throw RuntimeException("fail")
}

// FILE: 2.kt

import test.*

class Holder {
    var konstue: String = ""
}

fun test0(h: Holder): String {
    try {
        konst localResult = doCall (
                {
                    h.konstue += "OK_NON_LOCAL"
                    return "OK_NON_LOCAL"
                },
                {
                    h.konstue += ", OK_EXCEPTION"
                    "OK_EXCEPTION"
                },
                {
                    h.konstue += ", OK_FINALLY"
                    throw RuntimeException("FINALLY")
                    "OK_FINALLY"
                })

        return localResult;
    } catch (e: RuntimeException) {
        if (e.message != "FINALLY") {
            return "FAIL in exception: " + e.message
        } else {
            return "CATCHED_EXCEPTION"
        }
    }

    return "FAIL";
}

fun test1(h: Holder): String {
    try {
        konst localResult = doCall (
                {
                    h.konstue += "OK_LOCAL"
                    throw RuntimeException("FAIL")
                    "OK_LOCAL"
                },
                {
                    h.konstue += ", OK_EXCEPTION"
                    "OK_EXCEPTION"
                },
                {
                    h.konstue += ", OK_FINALLY"
                    throw RuntimeException("FINALLY")
                    "OK_FINALLY"
                })
    } catch (e: RuntimeException) {
        if (e.message != "FINALLY") {
            return "FAIL in exception: " + e.message
        } else {
            return "CATCHED_EXCEPTION"
        }
    }

    return "FAIL";
}

fun test2(h: Holder): String {

    konst localResult = doCall (
            {
                h.konstue += "OK_LOCAL"
                throw RuntimeException()
                "OK_LOCAL"
            },
            {
                h.konstue += ", OK_EXCEPTION"
                "OK_EXCEPTION"
            },
            {
                h.konstue += ", OK_FINALLY"
                "OK_FINALLY"
            })

    return localResult;
}

fun test3(h: Holder): String {
    try {
        konst localResult = doCall (
                {
                    h.konstue += "OK_LOCAL"
                    throw RuntimeException("FAIL")
                    "OK_LOCAL"
                },
                {
                    h.konstue += ", OK_EXCEPTION"
                    throw RuntimeException("FAIL_EX")
                    "OK_EXCEPTION"
                },
                {
                    h.konstue += ", OK_FINALLY"
                    throw RuntimeException("FINALLY")
                    "OK_FINALLY"
                })
    } catch (e: RuntimeException) {
        if (e.message != "FINALLY") {
            return "FAIL in exception: " + e.message
        } else {
            return "CATCHED_EXCEPTION"
        }
    }

    return "FAIL";
}

fun test4(h: Holder): String {
    try {
        konst localResult = doCall2 (
                {
                    h.konstue += "OK_LOCAL"
                    throw RuntimeException("FAIL")
                    "OK_LOCAL"
                },
                {
                    h.konstue += ", OK_EXCEPTION"
                    throw RuntimeException("EXCEPTION")
                    "OK_EXCEPTION"
                },
                {
                    h.konstue += ", OK_FINALLY"
                    "OK_FINALLY"
                })
    } catch (e: RuntimeException) {
        if (e.message != "EXCEPTION") {
            return "FAIL in exception: " + e.message
        } else {
            return "CATCHED_EXCEPTION"
        }
    }

    return "FAIL";
}




fun box(): String {
    var h = Holder()
    konst test0 = test0(h)
    if (test0 != "CATCHED_EXCEPTION" || h.konstue != "OK_NON_LOCAL, OK_FINALLY") return "test0: ${test0}, holder: ${h.konstue}"


    h = Holder()
    konst test1 = test1(h)
    if (test1 != "CATCHED_EXCEPTION" || h.konstue != "OK_LOCAL, OK_EXCEPTION, OK_FINALLY") return "test1: ${test1}, holder: ${h.konstue}"

    h = Holder()
    konst test2 = test2(h)
    if (test2 != "OK_FINALLY" || h.konstue != "OK_LOCAL, OK_EXCEPTION, OK_FINALLY") return "test2: ${test2}, holder: ${h.konstue}"


    h = Holder()
    konst test3 = test3(h)
    if (test3 != "CATCHED_EXCEPTION" || h.konstue != "OK_LOCAL, OK_EXCEPTION, OK_FINALLY") return "test3: ${test3}, holder: ${h.konstue}"

    h = Holder()
    konst test4 = test4(h)
    if (test4 != "CATCHED_EXCEPTION" || h.konstue != "OK_LOCAL, OK_EXCEPTION, OK_FINALLY") return "test4: ${test4}, holder: ${h.konstue}"

    return "OK"
}
