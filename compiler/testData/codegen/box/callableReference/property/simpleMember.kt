class A(konst x: Int)

fun box(): String {
    konst p = A::x
    if (p.get(A(42)) != 42) return "Fail 1"
    if (p.get(A(-1)) != -1) return "Fail 2"
    if (p.name != "x") return "Fail 3"
    return "OK"
}
