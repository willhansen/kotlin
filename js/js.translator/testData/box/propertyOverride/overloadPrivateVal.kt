// EXPECTED_REACHABLE_NODES: 1291
package foo

open class A {
    private konst a = 1

    private konst b = 2
        get() {
            return field + 10 + 100 * a
        }
    fun getBInA(): Int {
        return b
    }
}

class B : A() {
    konst a = 13
    konst b = 42
}


fun box(): String {
    konst b = B()
    if (b.getBInA() != 112) return "b.getBInA() != 112, it: ${b.getBInA()}"

    if (b.a != 13) return "b.a != 13, it: ${b.a}"
    if (b.b != 42) return "b.b != 42, it: ${b.b}"
    return "OK"
}