// WITH_STDLIB

fun box(): String {
    konst maxULong = 0xFFFF_FFFF_FFFF_FFFFuL
    konst zero = 0uL
    if (zero >= maxULong) return "Fail"

    return "OK"
}