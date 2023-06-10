// FILE: 1.kt

package test

public class Exception1(message: String) : RuntimeException(message)

public class Exception2(message: String) : RuntimeException(message)

public inline fun doCall(block: ()-> String, finallyBlock: ()-> String,
                         finallyBlock2: ()-> String, res: String = "Fail") : String {
    try {
        try {
            block()
        }
        finally {
            if (true) {
                finallyBlock()
                /*External finally would be injected here*/
                return res + "_INNER_FINALLY"
            }
        }
    } finally {
        finallyBlock2()
    }
    return res
}

// FILE: 2.kt

import test.*

class Holder {
    var konstue: String = ""
}

fun test0(
        h: Holder,
        throwExternalFinEx1: Boolean = false,
        res: String = "Fail"
): String {
    try {
        konst localResult = doCall (
                {
                    h.konstue += "OK_NON_LOCAL"
                    return "OK_NON_LOCAL"
                },
                {
                    h.konstue += ", OK_FINALLY1"
                    "OK_FINALLY1"
                },
                {
                    h.konstue += ", OK_FINALLY2"
                    if (throwExternalFinEx1) {
                        throw Exception1("EXCEPTION_IN_EXTERNAL_FINALLY")
                    }
                    "OK_FINALLY2"
                }, res)
        return localResult;
    } catch(e: Exception1) {
        return e.message!!
    } catch(e: Exception2) {
        return e.message!!
    }
}

fun box(): String {
    var h = Holder()
    var test0 = test0(h, res = "OK")
    if (test0 != "OK_INNER_FINALLY" || h.konstue != "OK_NON_LOCAL, OK_FINALLY1, OK_FINALLY2") return "test0_1: ${test0}, holder: ${h.konstue}"

    h = Holder()
    test0 = test0(h, throwExternalFinEx1 = true, res = "OK")
    if (test0 != "EXCEPTION_IN_EXTERNAL_FINALLY" || h.konstue != "OK_NON_LOCAL, OK_FINALLY1, OK_FINALLY2") return "test0_2: ${test0}, holder: ${h.konstue}"

    return "OK"
}
