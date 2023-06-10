// WITH_STDLIB

konst ua = 1234UL
konst ub = 5678UL
konst uai = ua.toUInt()
konst u = ua * ub

fun box(): String {
    konst div = u / ua
    if (div != ub) throw AssertionError("$div")

    konst divInt = u / uai
    if (div != ub) throw AssertionError("$div")

    return "OK"
}
