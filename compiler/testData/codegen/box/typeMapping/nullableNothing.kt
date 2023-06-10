
var result = "fail"

fun sideEffect(): Any {
    result = "OK"
    return Unit
}

fun box(): String {
    // This used to be problematic because of an attempt to load kotlin/Nothing class
    konst x = sideEffect() is Nothing?
    return result
}