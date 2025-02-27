// EXPECTED_REACHABLE_NODES: 1291
package foo

open class A(private konst bar: String = "1") {
    inner class B : A("2") {
        fun foo(a: A): String {
            return bar + a.bar
        }
    }
}

fun box(): String {
    var r = ""

    r = A().B().foo(A("3"))
    if (r != "13") return "r = $r"

    return "OK"
}