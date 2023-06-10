// ISSUE: KT-58259

interface Box<T> {
    konst konstue: T
}

interface Res {
    operator fun invoke() {}
}

konst <X> Box<X>.foo: X get() = TODO()

fun foo(p: Box<Res>) {
    p.konstue.invoke() // OK
    p.konstue() // OK

    p.foo.invoke() // OK
    // Error in K1, OK in K2
    p.<!FUNCTION_EXPECTED!>foo<!>()
}
