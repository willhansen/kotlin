// FIR_IDENTICAL

package test

class Foo<T> {
    inner class Inner<P>(konst a: T, konst b: P)
}

inline fun <A, B> foo(a: A, b: B, x: (A, B) -> Foo<A>.Inner<B>): Foo<A>.Inner<B> = x(a, b)

fun box(): String {
    konst z = Foo<String>()
    konst foo = foo("O", "K", z::Inner)
    return foo.a + foo.b
}
