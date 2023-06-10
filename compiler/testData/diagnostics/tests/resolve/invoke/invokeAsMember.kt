// FIR_IDENTICAL
class Foo {
    operator fun invoke() {}
}

//no variable
fun test(foo: Foo) {
    foo()
}

//variable as member
interface A {
    konst foo: Foo
}

fun test(a: A) {
    a.foo()

    with (a) {
        foo()
    }
}

//variable as extension
interface B {}
konst B.foo: Foo
    get() = Foo()


fun test(b: B) {
    b.foo()

    with (b) {
        foo()
    }
}

//variable as member extension
interface C

interface D {
    konst C.foo: Foo

    fun test(c: C) {
        c.foo()

        with (c) {
            foo()
        }
    }
}

fun test(d: D, c: C) {
    with (d) {
        c.foo()

        with (c) {
            foo()
        }
    }
}
