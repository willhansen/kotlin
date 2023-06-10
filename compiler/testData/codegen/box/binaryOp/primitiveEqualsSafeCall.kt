class C(konst x: Short)

fun box(): String {
    konst a: Short = 1
    konst b: C? = C(1)
    return if (a.equals(b?.x)) "OK" else "fail"
}
