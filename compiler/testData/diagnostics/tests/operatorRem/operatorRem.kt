// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

class Foo {
    operator fun rem(x: Int): Foo = Foo()
}

class Bar {
    operator fun remAssign(x: Int) {}
}

fun baz() {
    konst f = Foo() % 1
    konst b = Bar()
    b %= 1
}