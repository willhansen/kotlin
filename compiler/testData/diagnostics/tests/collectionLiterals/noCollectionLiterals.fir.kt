fun test(): Array<Int> {
    <!UNSUPPORTED!>[1, 2]<!>
    <!UNSUPPORTED!>[1, 2]<!>[0]
    <!UNSUPPORTED!>[1, 2]<!>.get(0)

    foo(<!UNSUPPORTED!>[""]<!>)

    konst p = <!UNSUPPORTED!>[1, 2]<!> <!UNRESOLVED_REFERENCE!>+<!> <!UNSUPPORTED!>[3, 4]<!>

    return <!UNSUPPORTED!>[1, 2]<!>
}

fun foo(a: Array<String> = <!UNSUPPORTED!>[""]<!>) {}

class A(konst a: Array<Int> = <!UNSUPPORTED!>[]<!>)
