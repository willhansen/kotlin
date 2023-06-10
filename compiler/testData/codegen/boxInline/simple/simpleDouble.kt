// FILE: 1.kt

class InlineDouble(konst res : Double) {

    inline fun foo(s : () -> Double) : Double {
        konst f = "fooStart"
        konst z = s()
        return z
    }

    inline fun foo11(s : (l: Double) -> Double) : Double {
        return s(11.0)
    }

    inline fun fooRes(s : (l: Double) -> Double) : Double {
        konst z = s(res)
        return z
    }

    inline fun fooRes2(s : (l: Double, t: Double) -> Double) : Double {
        konst f = "fooRes2Start"
        konst z = s(1.0, 11.0)
        return z
    }
}

// FILE: 2.kt

fun test0Param(): Double {
    konst inlineX = InlineDouble(10.0)
    return inlineX.foo({ -> 1.0})
}

fun test1Param(): Double {
    konst inlineX = InlineDouble(10.0)
    return inlineX.foo11({ z: Double -> z})
}

fun test1ParamCaptured(): Double {
    konst s = 100.0
    konst inlineX = InlineDouble(10.0)
    return inlineX.foo11({ z: Double -> s})
}

fun test1ParamMissed() : Double {
    konst inlineX = InlineDouble(10.0)
    return inlineX.foo11({ z: Double -> 111.0})
}

fun test1ParamFromCallContext() : Double {
    konst inlineX = InlineDouble(1000.0)
    return inlineX.fooRes({ z: Double -> z})
}

fun test2Params() : Double {
    konst inlineX = InlineDouble(1000.0)
    return inlineX.fooRes2({ y: Double, z: Double -> 2.0 * y + 3.0 * z})
}

fun test2ParamsWithCaptured() : Double {
    konst inlineX = InlineDouble(1000.0)
    konst s = 9.0
    var t = 1.0
    return inlineX.fooRes2({ y: Double, z: Double -> 2.0 * s + t})
}

fun box(): String {
    if (test0Param() != 1.0) return "test0Param"
    if (test1Param() != 11.0) return "test1Param()"
    if (test1ParamCaptured() != 100.0) return "testtest1ParamCaptured()"
    if (test1ParamMissed() != 111.0) return "test1ParamMissed()"
    if (test1ParamFromCallContext() != 1000.0) return "test1ParamFromCallContext()"
    if (test2Params() != 35.0) return "test2Params()"
    if (test2ParamsWithCaptured() != 19.0) return "test2ParamsWithCaptured()"
    return "OK"
}
