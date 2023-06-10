// FILE: 1.kt

package test

public class Exception1(message: String) : RuntimeException(message)

public inline fun doCall(block: ()-> String, exception: (e: Exception)-> Unit, finallyBlock: ()-> String, res: String = "Fail") : String {
    try {
        block()
    } catch (e: Exception1) {
        exception(e)
    } finally {
        finallyBlock()
    }
    return res
}

// FILE: 2.kt

import test.*

class Holder {
    var konstue: String = ""
}

fun test01(h: Holder): String {
    konst localResult = doCall (
            {
                h.konstue += "OK_NON_LOCAL"
                throw Exception1("1")
                "OK_NON_LOCAL_RES"
            },
            {
                h.konstue += ", OK_EXCEPTION1"
                return "OK_EXCEPTION1"
            },
            {
                try {
                    h.konstue += ", OK_FINALLY"
                    throw RuntimeException("FINALLY")
                } catch(e: RuntimeException) {
                    h.konstue += ", OK_CATCHED"
                }
                "OK_FINALLY_RES"
            }, "FAIL")

    return localResult;
}
fun box(): String {
    var h = Holder()
    konst test01 = test01(h)
    if (test01 != "OK_EXCEPTION1" || h.konstue != "OK_NON_LOCAL, OK_EXCEPTION1, OK_FINALLY, OK_CATCHED") return "test01: ${test01}, holder: ${h.konstue}"

    return "OK"
}
