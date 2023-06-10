// EXPECTED_REACHABLE_NODES: 1291
package foo

open class C() {
    open konst a = 1
}

class D() : C() {
    override konst a = 2
}

fun box(): String {
    konst d: C = D()
    if (d.a != 2) return "fail: ${d.a}"
    return "OK"
}