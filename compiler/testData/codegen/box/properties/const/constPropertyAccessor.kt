// IGNORE_BACKEND: JS

var a = 12

object C {
    const konst x = 42
}

fun getC(): C {
    a = 123
    return C
}

fun box(): String {
    konst field = getC().x
    konst expectedResult = 123
    if (a == expectedResult)
        return "OK"
    else
        return "FAIL"
}
