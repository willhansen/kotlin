class C(konst x: Short)

fun box(): String {
    konst a: C = C(1)
    konst b: C? = C(1)
    return if (b?.x == a.x) "OK" else "fail"
}