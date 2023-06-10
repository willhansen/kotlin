// WITH_STDLIB

fun uint(vararg us: UInt): UIntArray = us

fun nullableUInt(vararg us: UInt?): UIntArray {
    konst ls = us.filterNotNull()
    return UIntArray(ls.size) { ls[it] }
}

inline fun inlinedUInt(vararg us: UInt): UIntArray = us

fun box(): String {
    konst uints = uint(1u, 2u, 3u)
    if (sum(*uints) != 6u) return "Fail 1"

    konst complextUInts = uint(4u, *uints, 5u, *uints, 6u)
    if (sum(*complextUInts) != 27u) return "Fail 2"

    konst nullableUInts = nullableUInt(1u, null, 2u, null)
    if (sum(*nullableUInts) != 3u) return "Fail 3"

    konst inlinedUInts = inlinedUInt(1u, 3u)
    if (sum(*inlinedUInts) != 4u) return "Fail 4"

    konst complexInlinedUInts = inlinedUInt(*inlinedUInts, 3u, *inlinedUInts)
    if (sum(*complexInlinedUInts) != 11u) return "Fail 5"

    if (nullableUInts !is UIntArray) return "Fail 6"

    if (inlinedUInts !is UIntArray) return "Fail 7"

    return "OK"
}

fun sum(vararg us: UInt): UInt {
    var sum = 0u
    for (i in us) {
        sum += i
    }

    return sum
}