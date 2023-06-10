// !DIAGNOSTICS: -UNUSED_VARIABLE
package test

object Wrong
object Right

class a {
    class b<T> {
        class c {
            fun foo() = Wrong
        }
    }
}

fun Int.foo() = Right

class Test {
    konst a: List<Int> = null!!

    konst <T> List<T>.b: Int get() = 42

    konst Int.c: Int get() = 42

    konst test1: () -> Right = a.<!DEBUG_INFO_LEAKING_THIS!>b<!>.<!DEBUG_INFO_LEAKING_THIS!>c<!>::foo
    konst test1a: () -> Right = <!RESERVED_SYNTAX_IN_CALLABLE_REFERENCE_LHS!><!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>b<!><<!DEBUG_INFO_MISSING_UNRESOLVED!>Int<!>>.<!DEBUG_INFO_MISSING_UNRESOLVED!>c<!><!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>

    konst test2: () -> Right = <!RESERVED_SYNTAX_IN_CALLABLE_REFERENCE_LHS!>a.<!DEBUG_INFO_LEAKING_THIS!>b<!>.<!DEBUG_INFO_LEAKING_THIS!>c<!><!>?::foo
    konst test2a: () -> Right = <!RESERVED_SYNTAX_IN_CALLABLE_REFERENCE_LHS!><!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>b<!><<!DEBUG_INFO_MISSING_UNRESOLVED!>Int<!>>.<!DEBUG_INFO_MISSING_UNRESOLVED!>c<!><!>?::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>
}
