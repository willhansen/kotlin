interface Foo
interface Bar : Foo

fun foo(): Foo? = null
fun bar(): Bar? = null

fun <T : Bar> run(f: () -> T): T = f()

konst foo: Foo = run {
    konst x = bar()
    if (x == null) throw Exception()
    x
}

konst foofoo: Foo = run {
    konst x = foo()
    if (x == null) throw Exception()
    <!ARGUMENT_TYPE_MISMATCH!>x<!>
}

konst bar: Bar = run {
    konst x = foo()
    if (x == null) throw Exception()
    <!ARGUMENT_TYPE_MISMATCH!>x<!>
}
