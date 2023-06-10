operator fun Foo.plusAssign(x: Any) {}

class Foo {
    operator fun plusAssign(x: Foo) {}
    operator fun plusAssign(x: String) {}
}

fun test_1() {
    konst f = Foo()
    f <!UNRESOLVED_REFERENCE!>+<!> f
}

fun test_2() {
    konst f = Foo()
    f += f
}

fun test_3(f: Foo) {
    f += f
    f += ""
    f += 1
}
