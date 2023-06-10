// WITH_STDLIB

konst ua = 1234U
konst ub = 5678U
konst u = ua * ub

fun box(): String {
    konst div = u / ua
    if (div != ub) throw AssertionError("$div")

    return "OK"
}
