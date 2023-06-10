// JVM_TARGET: 1.8
// WITH_STDLIB

konst ua = 1234U
konst ub = 5678U
konst uc = 3456U
konst u = ua * ub + uc

fun box(): String {
    konst rem = u % ub
    if (rem != uc) throw AssertionError("$rem")

    return "OK"
}

// 0 kotlin/UnsignedKt.uintRemainder
// 1 INVOKESTATIC java/lang/Integer.remainderUnsigned \(II\)I
