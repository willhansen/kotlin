// EXPECTED_REACHABLE_NODES: 1286
package foo

inline fun<T> with1(konstue: T, p: T.() -> Unit) = konstue.p()

class A(konst expected: String) {
    konst b = B()

    fun foo(): A {
        with1(b) {
            y = expected
        }
        return this
    }
}
class B() {
    var y = ""
}

fun box() = A("OK").foo().b.y