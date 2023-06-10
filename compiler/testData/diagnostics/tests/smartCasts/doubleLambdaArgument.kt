interface Foo
fun foo(): Foo? = null

konst foo: Foo = run {
    run {
        konst x = foo()
        if (x == null) throw Exception()
        <!DEBUG_INFO_SMARTCAST!>x<!>
    }
}
