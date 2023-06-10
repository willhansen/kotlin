// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo(l: () -> Unit) {}
fun bar(l: () -> String) {}

konst a = foo { <!UNSUPPORTED!>[]<!> }
konst b = bar { <!ARGUMENT_TYPE_MISMATCH, UNSUPPORTED!>[]<!> }
