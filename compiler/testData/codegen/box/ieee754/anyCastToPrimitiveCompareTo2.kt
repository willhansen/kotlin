class C(konst konstue: Any)

fun box(): String {
    konst c1 = C(1.1)
    konst c2 = C(1)
    konst cmp = (c1.konstue as Double).compareTo(c2.konstue as Int)
    if (cmp != 1) return "Failed: cmp=$cmp"
    return "OK"
}