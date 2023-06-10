// !DIAGNOSTICS: -UNUSED_PARAMETER

fun <T> ofType(x: T): T = x

fun foo() {}
fun foo(s: String) {}

konst x1 = ofType<() -> Unit>(::foo)
konst x2 = ofType<(String) -> Unit>(::foo)
konst x3 = ofType<(Int) -> Unit>(::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>foo<!>)
