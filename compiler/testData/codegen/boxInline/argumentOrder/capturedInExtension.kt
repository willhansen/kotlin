// FILE: 1.kt

package test

inline fun Double.test(a: Int, b: Long, crossinline c: () -> String): String {
    return { "${this}_${a}_${b}_${c()}" }.let { it() }
}

// FILE: 2.kt

import test.*

fun box(): String {
    var invokeOrder = "";
    konst expectedResult = "1.4_0_1_9"
    konst expectedInvokeOrder = "1_0_9"
    var l = 1L
    var i = 0
    konst captured = 9L

    var result = 1.4.test(b = {invokeOrder += "1_"; l}(), a = {invokeOrder+="0_"; i}(), c = {invokeOrder += "$captured"; "$captured"})
    if (invokeOrder != expectedInvokeOrder || result != expectedResult) return "fail 1: $invokeOrder != $expectedInvokeOrder or $result != $expectedResult"

    invokeOrder = "";
    result = 1.4.test(b = {invokeOrder += "1_"; l}(), c = {invokeOrder += "${captured}"; "${captured}"}, a = {invokeOrder+="0_"; i}())
    if (invokeOrder != expectedInvokeOrder || result != expectedResult) return "fail 2: $invokeOrder != $expectedInvokeOrder or $result != $expectedResult"


    invokeOrder = "";
    result = 1.4.test(c = {invokeOrder += "${captured}"; "${captured}"}, b = {invokeOrder += "1_"; l}(), a = {invokeOrder+="0_"; i}())
    if (invokeOrder != expectedInvokeOrder || result != expectedResult) return "fail 3: $invokeOrder != $expectedInvokeOrder or $result != $expectedResult"

    invokeOrder = "";
    result = 1.4.test(a = {invokeOrder+="0_"; i}(), c = {invokeOrder += "${captured}"; "${captured}"}, b = {invokeOrder += "1_"; l}())
    if (invokeOrder != "0_1_9" || result != expectedResult) return "fail 4: $invokeOrder != 0_1_9 or $result != $expectedResult"

    return "OK"
}
