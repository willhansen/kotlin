// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER


interface Bar<T> {
    konst t: T
}

class MyBar<T>(override konst t: T) : Bar<T>

class BarR : Bar<BarR> {
    override konst t: BarR get() = this
}

class Foo<F : Bar<F>>(konst f: F)

fun <T> id(t1: T, t2: T) = t2

fun test(foo: Foo<*>, g: Bar<*>) {
    id(foo.f, g).t.<!UNRESOLVED_REFERENCE!>t<!>
}

fun main() {
    konst foo = Foo(BarR())
    test(foo, MyBar(2))
}
