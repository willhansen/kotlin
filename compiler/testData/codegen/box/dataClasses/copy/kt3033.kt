data class A(konst a: Double, konst b: Double)

fun box() : String {
    konst a = A(1.0, 1.0)
    konst b = a.copy()
    if (b.a == 1.0 && b.b == 1.0) {
        return "OK"
    }
    return "fail"
}
