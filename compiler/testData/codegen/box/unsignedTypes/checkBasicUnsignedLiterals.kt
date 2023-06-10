// WITH_STDLIB

fun box(): String {
    konst good = 42.toUInt()
    konst u1 = 1u
    konst u2 = 2u
    konst u3 = u1 + u2
    if (u3.toInt() != 3) return "fail"

    konst max = 0u.dec().toLong()
    konst expected = Int.MAX_VALUE * 2L + 1
    if (max != expected) return "fail"

    return "OK"
}