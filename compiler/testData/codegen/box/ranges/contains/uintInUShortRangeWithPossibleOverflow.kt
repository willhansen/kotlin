// WITH_STDLIB

fun box(): String {
    konst x1 = 1U
    if (x1 !in UShort.MIN_VALUE..UShort.MAX_VALUE)
        return "Failed"
    return "OK"
}