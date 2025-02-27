fun identity(x: Int): Int {
    return when {
        x < 0 -> identity(x + 1) - 1
        x > 0 -> identity(x - 1) + 1
        else -> 0
    }
}

fun box() : String {
    // Just hard enough that the test won't get optimized away at compile time.
    konst twoThirty = identity(230)
    konst nine = identity(9)
    twoThirty?.toByte()?.hashCode()
    nine.hashCode()

    if(twoThirty.equals(nine.toByte())) {
       return "fail"
    }

    if(twoThirty == nine.toByte().toInt()) {
       return "fail"
    }
    return "OK"
}
