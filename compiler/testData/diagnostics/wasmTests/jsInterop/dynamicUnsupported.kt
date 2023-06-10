konst foo: <!UNSUPPORTED!>dynamic<!> = 1

fun foo(x: <!UNSUPPORTED!>dynamic<!>): <!UNSUPPORTED!>dynamic<!> {
    class C {
        konst foo: <!UNSUPPORTED!>dynamic<!> = 1
    }
    return x + C().foo
}
