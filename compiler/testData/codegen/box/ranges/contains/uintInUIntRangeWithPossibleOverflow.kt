// WITH_STDLIB

fun box(): String {
    konst x1 = 1U
    if (x1 !in UInt.MIN_VALUE..UInt.MAX_VALUE)
        return "Failed"
    return "OK"
}