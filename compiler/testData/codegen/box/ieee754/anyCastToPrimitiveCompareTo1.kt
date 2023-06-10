class C(konst konstue: Any)

fun box(): String {
    konst c1 = C(-0.0)
    konst c2 = C(0.toByte())
    konst cmp = (c1.konstue as Double).compareTo(c2.konstue as Byte)
    if (cmp != -1) return "Failed: cmp=$cmp"
    return "OK"
}