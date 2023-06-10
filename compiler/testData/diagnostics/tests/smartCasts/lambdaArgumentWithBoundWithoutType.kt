interface Foo
interface Bar : Foo

fun foo(): Foo? = null
fun bar(): Bar? = null

fun <T : Bar> run(f: () -> T): T = f()

konst foo: Foo = run {
    konst x = bar()
    if (x == null) throw Exception()
    <!DEBUG_INFO_SMARTCAST!>x<!>
}

konst foofoo: Foo = run {
    konst x = foo()
    if (x == null) throw Exception()
    <!DEBUG_INFO_SMARTCAST, TYPE_MISMATCH!>x<!>
}

konst bar: Bar = <!TYPE_MISMATCH!>run {
    konst x = foo()
    if (x == null) throw Exception()
    <!DEBUG_INFO_SMARTCAST, TYPE_MISMATCH!>x<!>
}<!>
