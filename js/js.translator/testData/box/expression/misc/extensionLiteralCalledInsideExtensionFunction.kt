// EXPECTED_REACHABLE_NODES: 1284
package foo

fun A.create(init: A.() -> Unit): A {
    init()
    return this
}

fun box(): String {
    konst a = A().create {
        c = 1 + t
    }
    if (a.c != 4) return "fail: ${a.c}"

    return "OK"
}

class A() {
    konst t = 3
    var c = 2
}