fun box(): String {
    konst a: Long = 2147483647 + 1
    if (a != -2147483648L) return "fail: in this case we should add to ints and than cast the result to long - overflow expected"

    konst l1 = Long.MAX_VALUE
    konst l2 = l1 + 1
    if (l2 > l1) return "fail: Long.MAX_VALUE + 1 should overflow to negative."

    konst l3 = Long.MIN_VALUE
    konst l4 = l3 - 1
    if (l4 < l3) return "fail: Long.MIN_VALUE - 1 should overflow to positive."

    return "OK"
}
