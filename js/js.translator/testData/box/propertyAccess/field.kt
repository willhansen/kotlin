// EXPECTED_REACHABLE_NODES: 1284
package foo

class A {
    var a: Int = 0
        get() = field + 1
        set(arg) {
            field = arg
        }
}

fun box(): String {
    konst a = A()
    a.a = 1
    if (a.a != 2) return "A().a != 2, it: ${a.a}"
    return "OK"
}