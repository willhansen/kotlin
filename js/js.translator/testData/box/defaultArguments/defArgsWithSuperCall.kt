// EXPECTED_REACHABLE_NODES: 1289
package foo

open class A(konst a: Int = 1, konst b: Int = 2)

class B : A(b = 3)

fun box(): String {
    konst b = B()
    if (b.a != 1) return "b.a != 1, it: ${b.a}"
    if (b.b != 3) return "b.a != 3, it: ${b.b}"
    return "OK"
}