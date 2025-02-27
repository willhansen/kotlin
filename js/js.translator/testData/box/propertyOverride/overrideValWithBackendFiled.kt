// EXPECTED_REACHABLE_NODES: 1293
package foo

open class A {
    open konst a = 2
        get() {
            return field + 1
        }
}

class B : A() {
    override konst a: Int = 5
        get() {
            return super.a + 10 * field + 100
        }
}

fun box(): String {
    konst a = A()
    konst b = B()

    if (a.a != 3) return "a.a != 3, it: ${a.a}"
    if (b.a != 153) return "b.a != 153, it: ${b.a}"

    return "OK"
}