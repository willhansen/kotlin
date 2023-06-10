// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER

interface A
fun devNull(a: Any?){}

konst generic_fun = fun<!TYPE_PARAMETERS_NOT_ALLOWED!><T><!>(t: <!UNRESOLVED_REFERENCE!>T<!>): <!UNRESOLVED_REFERENCE!>T<!> = null!!
konst extension_generic_fun = fun<!TYPE_PARAMETERS_NOT_ALLOWED!><T><!><!UNRESOLVED_REFERENCE!>T<!>.(t: <!UNRESOLVED_REFERENCE!>T<!>): <!UNRESOLVED_REFERENCE!>T<!> = null!!

fun fun_with_where() = fun <!TYPE_PARAMETERS_NOT_ALLOWED!><T><!> <!UNRESOLVED_REFERENCE!>T<!>.(t: <!UNRESOLVED_REFERENCE!>T<!>): <!UNRESOLVED_REFERENCE!>T<!> where T: A = null!!


fun outer() {
    devNull(fun <!TYPE_PARAMETERS_NOT_ALLOWED!><T><!>() {})
    devNull(fun <!TYPE_PARAMETERS_NOT_ALLOWED!><T><!> <!UNRESOLVED_REFERENCE!>T<!>.() {})
    devNull(fun <!TYPE_PARAMETERS_NOT_ALLOWED!><T><!> (): <!UNRESOLVED_REFERENCE!>T<!> = null!!)
    devNull(fun <!TYPE_PARAMETERS_NOT_ALLOWED!><T><!> (t: <!UNRESOLVED_REFERENCE!>T<!>) {})
    devNull(fun <!TYPE_PARAMETERS_NOT_ALLOWED!><T><!> () where T:A {})
}
