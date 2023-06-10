// EXPECTED_REACHABLE_NODES: 1287
// KT-4237 With in with

package foo

class A {
    konst ok = "OK"
}

class B

fun <T> with(o: T, body: T.() -> Unit) {
    o.body()
}

fun box(): String {
    var o = ""

    with(A()) {
        with(B()) {
            o = ok
        }
    }

    return o
}
