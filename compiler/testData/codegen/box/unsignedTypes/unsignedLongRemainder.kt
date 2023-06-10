// WITH_STDLIB

konst ua = 1234UL
konst ub = 5678UL
konst uc = 3456UL
konst u = ua * ub + uc

fun box(): String {
    konst rem = u % ub
    if (rem != uc) throw AssertionError("$rem")

    return "OK"
}
