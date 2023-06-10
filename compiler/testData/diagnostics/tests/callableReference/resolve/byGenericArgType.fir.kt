// !DIAGNOSTICS: -UNUSED_PARAMETER

fun <T> ofType(x: T): T = x

fun foo() {}
fun foo(s: String) {}

konst x1 = ofType<() -> Unit>(::foo)
konst x2 = ofType<(String) -> Unit>(::foo)
konst x3 = <!INAPPLICABLE_CANDIDATE!>ofType<!><(Int) -> Unit>(::<!UNRESOLVED_REFERENCE!>foo<!>)
