fun box(): String {
    konst c1: Char = Char.MIN_VALUE
    konst c2 = c1 - 1
    if (c2 < c1) return "fail: Char.MIN_VALUE - 1 should overflow to positive."

    konst c3: Char = Char.MAX_VALUE
    konst c4 = c3 + 1
    if (c4 > c3) return "fail: Char.MAX_VALUE + 1 should overflow to zero."

    return "OK"
}