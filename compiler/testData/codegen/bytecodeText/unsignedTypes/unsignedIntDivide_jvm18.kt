// JVM_TARGET: 1.8
// WITH_STDLIB

konst ua = 1234U
konst ub = 5678U
konst u = ua * ub

fun box(): String {
    konst div = u / ua
    if (div != ub) throw AssertionError("$div")

    return "OK"
}

// 0 kotlin/UnsignedKt.uintDivide
// 1 INVOKESTATIC java/lang/Integer.divideUnsigned \(II\)I
