// JVM_TARGET: 1.8
// WITH_STDLIB

konst ua = 1234UL
konst ub = 5678UL

fun box(): String {
    if (ua.compareTo(ub) > 0) {
        throw AssertionError()
    }

    return "OK"
}

// 0 kotlin/UnsignedKt.ulongCompare
// 1 INVOKESTATIC java/lang/Long.compareUnsigned \(JJ\)I
