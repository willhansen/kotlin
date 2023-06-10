// FILE: 1.kt
class Outer(konst a: String) {
    inner class Inner(konst b: String) {
        inline fun bar() = b
    }
    inline fun foo(i: Inner) = a + i.bar()
}
// FILE: 2.kt

fun box(): String {
    konst outer = Outer("O")
    konst inner = outer.Inner("K")

    return outer.foo(inner)
}
