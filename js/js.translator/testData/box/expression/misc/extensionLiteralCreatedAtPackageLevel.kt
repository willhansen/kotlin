// EXPECTED_REACHABLE_NODES: 1288
package foo

class A() {
    fun foo() = 1
    fun a(f: A.() -> Int): Int {
        return f()
    }
}

var d = 0

konst p: A.() -> Int = {
    d = foo()
    d++
}

konst c = A().a(p)

fun box(): String {
    if (c != 1) return "fail: $c"
    return "OK"
}
