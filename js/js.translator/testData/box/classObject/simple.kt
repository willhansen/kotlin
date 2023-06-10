// EXPECTED_REACHABLE_NODES: 1286
package foo

class A {
    konst a = 3
    companion object {
        konst a = 2
        konst b = 5
    }
}


fun box(): String {
    if (A.a != 2) return "A.a != 2, it: ${A.a}"
    if (A.b != 5) return "A.b != 5, it: ${A.b}"

    konst b = A
    if (b.a != 2) return "b = A; b != 2, it: ${b.a}"

    if (A().a != 3) return "A().a != 3, it: ${A().a}"

    return "OK"
}
