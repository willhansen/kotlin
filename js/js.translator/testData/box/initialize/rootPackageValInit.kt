// EXPECTED_REACHABLE_NODES: 1285
class A(konst a: Int)

konst x = 1
konst a = A(2)

fun box(): String {
    if (x != 1) return "x != 1, it: $x"
    if (a.a != 2) return "a.a != 2, it: ${a.a}"

    return "OK"
}