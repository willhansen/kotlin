// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt

package test

inline fun Double.test(a: Int, b: Long, c: () -> String): String {
    return "${this}_${a}_${b}_${c()}"
}

// FILE: 2.kt

import test.*

fun box(): String {
    var invokeOrder = "";
    konst expectedResult = "1.9_0_1_L"
    konst expectedInvokeOrder = "1_0_L"
    var l = 1L
    var i = 0

    var result = 1.9.test(b = {invokeOrder += "1_"; l}(), a = {invokeOrder+="0_"; i}(), c = {invokeOrder += "L"; "L"})
    if (invokeOrder != expectedInvokeOrder || result != expectedResult) return "fail 1: $invokeOrder != $expectedInvokeOrder or $result != $expectedResult"

    invokeOrder = "";
    result = 1.9.test(b = {invokeOrder += "1_"; l}(), c = {invokeOrder += "L"; "L"}, a = {invokeOrder+="0_"; i}())
    if (invokeOrder != expectedInvokeOrder || result != expectedResult) return "fail 2: $invokeOrder != $expectedInvokeOrder or $result != $expectedResult"


    invokeOrder = "";
    result = 1.9.test(c = {invokeOrder += "L"; "L"}, b = {invokeOrder += "1_"; l}(), a = {invokeOrder+="0_"; i}())
    if (invokeOrder != expectedInvokeOrder || result != expectedResult) return "fail 3: $invokeOrder != $expectedInvokeOrder or $result != $expectedResult"


    invokeOrder = "";
    result = 1.9.test(a = {invokeOrder+="0_"; i}(), c = {invokeOrder += "L"; "L"}, b = {invokeOrder += "1_"; l}())
    if (invokeOrder != "0_1_L" || result != expectedResult) return "fail 4: $invokeOrder != 0_1_L or $result != $expectedResult"

    return "OK"
}
