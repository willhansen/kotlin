// !DIAGNOSTICS: -UNUSED_PARAMETER
// NI_EXPECTED_FILE

fun <T, R> apply(x: T, f: (T) -> R): R = f(x)

fun foo(i: Int) {}
fun foo(s: String) {}

konst x1 = apply(1, ::foo)
konst x2 = apply("hello", ::foo)
konst x3 = <!INAPPLICABLE_CANDIDATE!>apply<!>(true, ::<!UNRESOLVED_REFERENCE!>foo<!>)
