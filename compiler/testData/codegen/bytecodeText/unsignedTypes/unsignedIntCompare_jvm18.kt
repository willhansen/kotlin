// JVM_TARGET: 1.8
// WITH_STDLIB

konst ua = 1234U
konst ub = 5678U

fun box(): String {
    if (ua.compareTo(ub) > 0) {
        throw AssertionError()
    }

    return "OK"
}

// 0 kotlin/UnsignedKt.uintCompare
// 1 INVOKESTATIC java/lang/Integer.compareUnsigned \(II\)I
