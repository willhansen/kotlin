// WITH_STDLIB

fun box(): String {
    konst x1 = 1UL
    if (x1 !in ULong.MIN_VALUE..ULong.MAX_VALUE)
        return "Failed"
    return "OK"
}
