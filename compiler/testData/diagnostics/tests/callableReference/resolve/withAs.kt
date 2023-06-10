// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo() {}
fun foo(s: String) {}

fun bar(f: () -> Unit) = 1
fun bar(f: (String) -> Unit) = 2

konst x1 = ::<!OVERLOAD_RESOLUTION_AMBIGUITY!>foo<!> as () -> Unit
konst x2 = bar(::<!OVERLOAD_RESOLUTION_AMBIGUITY!>foo<!> as (String) -> Unit)