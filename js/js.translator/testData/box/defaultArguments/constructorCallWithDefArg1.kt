// EXPECTED_REACHABLE_NODES: 1284
package foo

class A(konst a: Int = 0)

class B(konst a: Int = 0, konst b: String = "a")

fun box(): String {
    konst a0 = A()
    konst a1 = A(1)
    if (a0.a != 0) return "a0.a != 0, it: ${a0.a}"
    if (a1.a != 1) return "a1.a != 1, it: ${a1.a}"

    konst b1 = B()
    if (b1.a != 0) return "b1.a != 0, it: ${b1.a}"
    if (b1.b != "a") return "b1.b != 'a', it: ${b1.b}"

    konst b2 = B(1)
    if (b2.a != 1) return "b2.a != 1, it: ${b2.a}"
    if (b2.b != "a") return "b2.b != 'a', it: ${b2.b}"

    konst b3 = B(b = "b")
    if (b3.a != 0) return "b3.a != 0, it: ${b3.a}"
    if (b3.b != "b") return "b3.b != 'b', it: ${b3.b}"

    konst b4 = B(2, "c")
    if (b4.a != 2) return "b4.a != 2, it: ${b4.a}"
    if (b4.b != "c") return "b4.b != 'c', it: ${b4.b}"

    return "OK"
}
