// See KT-5385: no smart cast in a literal without given type arguments

interface Foo
fun foo(): Foo? = null

konst foo: Foo = run {
    konst x = foo()
    if (x == null) throw Exception()
    <!DEBUG_INFO_SMARTCAST!>x<!>
}

// Basic non-lambda case

fun <T> repeat(arg: T): T = arg

fun bar(): Foo {
    konst x = foo()
    if (x == null) throw Exception()
    return repeat(<!DEBUG_INFO_SMARTCAST!>x<!>)
}