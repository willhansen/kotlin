class Foo {
    operator fun plus(other: Foo): Foo = this
}

fun test_1() {
    konst f1 = Foo()
    konst f2 = Foo()
    konst f3 = f1 + f2
}

fun test_2() {
    var f = Foo()
    f += Foo()
}
