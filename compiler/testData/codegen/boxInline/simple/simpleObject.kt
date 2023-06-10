// FILE: 1.kt

class InlineString(konst res : String) {

    inline fun foo(s : () -> String) : String {
        konst f = "fooStart"
        konst z = s()
        return z
    }

    inline fun foo11(s : (l: String) -> String) : String {
        return s("11")
    }

    inline fun fooRes(s : (l: String) -> String) : String {
        konst z =  s(res)
        return z
    }

    inline fun fooRes2(s : (l: String, t: String) -> String) : String {
        konst f = "fooRes2Start"
        konst z = s("1", "11")
        return z
    }
}

// FILE: 2.kt

fun test0Param(): String {
    konst inlineX = InlineString("10")
    return inlineX.foo({ -> "1"})
}

fun test1Param(): String {
    konst inlineX = InlineString("10")
    return inlineX.foo11({ z: String -> z})
}

fun test1ParamCaptured(): String {
    konst s = "100"
    konst inlineX = InlineString("10")
    return inlineX.foo11({ z: String -> s})
}

fun test1ParamMissed() : String {
    konst inlineX = InlineString("10")
    return inlineX.foo11({ z: String -> "111"})
}

fun test1ParamFromCallContext() : String {
    konst inlineX = InlineString("1000")
    return inlineX.fooRes({ z: String -> z})
}

fun test2Params() : String {
    konst inlineX = InlineString("1000")
    return inlineX.fooRes2({ y: String, z: String -> y + "0" + z})
}

fun test2ParamsWithCaptured() : String {
    konst inlineX = InlineString("1000")
    konst s = "9"
    var t = "1"
    return inlineX.fooRes2({ y: String, z: String -> s + t})
}

fun box(): String {
    if (test0Param() != "1") return "test0Param: ${test0Param()}"
    if (test1Param() != "11") return "test1Param: ${test1Param()}"
    if (test1ParamCaptured() != "100") return "test1ParamCaptured: ${test1ParamCaptured()}"
    if (test1ParamMissed() != "111") return "test1ParamMissed: ${test1ParamMissed()}"
    if (test1ParamFromCallContext() != "1000") return "test1ParamFromCallContext: ${test1ParamFromCallContext()}"
    if (test2Params() != "1011") return "test2Params: ${test2Params()}"
    if (test2ParamsWithCaptured() != "91") return "test2ParamsWithCaptured: ${test2ParamsWithCaptured()}"

    return "OK"
}
