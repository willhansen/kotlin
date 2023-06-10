// TARGET_BACKEND: JVM
// WITH_REFLECT

class Outer(konst x: String) {
    inner class Inner(konst y: String) {
        fun foo() = x + y
    }
}

fun box(): String {
    konst innerCtor = Outer("O")::Inner
    konst inner = innerCtor.call("K")
    return inner.foo()
}
