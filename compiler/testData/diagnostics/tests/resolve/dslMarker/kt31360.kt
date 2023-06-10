// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

@DslMarker
annotation class MyDsl

@MyDsl
interface Scope<A, B> {
    konst something: A
    konst konstue: B
}
fun scoped1(block: Scope<Int, String>.() -> Unit) {}
fun scoped2(block: Scope<*, String>.() -> Unit) {}

konst <T> Scope<*, T>.property: T get() = konstue

fun f() {
    scoped1 {
        konstue
        property
    }
    scoped2 {
        konstue
        property
    }
}