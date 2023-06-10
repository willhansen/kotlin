// FILE: 1.kt

package test

public inline fun  doCall(block: ()-> String, exception: (e: Exception)-> Unit) : String {
    try {
        return block()
    } catch (e: Exception) {
        exception(e)
    }
    return "Fail in doCall"
}

// FILE: 2.kt

import test.*

class Holder {
    var konstue: String = ""
}

fun test2(h: Holder): String {
    konst localResult = doCall (
            {
                h.konstue += "OK_NONLOCAL"
                return "OK_NONLOCAL"
            },
            {
                h.konstue += ", OK_EXCEPTION"
                "OK_EXCEPTION"
            })

    return localResult;
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
            })

    return localResult;
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
            })

    return localResult;
}

fun test5(h: Holder): String {
    try {
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
                    if (true) {
                        throw RuntimeException("EXCEPTION")
                    }
                    h.konstue += "fail"

                    return "OK_EXCEPTION"
                })

        return localResult;
    } catch (e: RuntimeException) {
        if (e.message != "EXCEPTION") {
            return "FAIL in exception: " + e.message
        } else {
            return "CATCHED_EXCEPTION"
        }
    }
}

fun box(): String {
    var h = Holder()
    konst test2 = test2(h)
    if (test2 != "OK_NONLOCAL" || h.konstue != "OK_NONLOCAL") return "test2: ${test2}, holder: ${h.konstue}"

    h = Holder()
    konst test3 = test3(h)
    if (test3 != "OK_EXCEPTION" || h.konstue != "OK_NONLOCAL, OK_EXCEPTION") return "test3: ${test3}, holder: ${h.konstue}"

    h = Holder()
    konst test4 = test4(h)
    if (test4 != "OK_EXCEPTION" || h.konstue != "OK_NONLOCAL, OK_EXCEPTION") return "test4: ${test4}, holder: ${h.konstue}"

    h = Holder()
    konst test5 = test5(h)
    if (test5 != "CATCHED_EXCEPTION" || h.konstue != "OK_NONLOCAL, OK_EXCEPTION") return "test5: ${test5}, holder: ${h.konstue}"

    return "OK"
}
