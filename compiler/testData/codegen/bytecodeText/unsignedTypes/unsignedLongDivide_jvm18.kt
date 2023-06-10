// JVM_TARGET: 1.8
// WITH_STDLIB

konst ua = 1234UL
konst ub = 5678UL
konst u = ua * ub

fun box(): String {
    konst div = u / ua
    if (div != ub) throw AssertionError("$div")

    return "OK"
}

// 0 kotlin/UnsignedKt.ulongDivide
// 1 INVOKESTATIC java/lang/Long.divideUnsigned \(JJ\)J