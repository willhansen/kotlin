// !DIAGNOSTICS: -UNUSED_PARAMETER
// NI_EXPECTED_FILE

fun <T, R> apply(x: T, f: (T) -> R): R = f(x)

fun foo(i: Int) {}
fun foo(s: String) {}

konst x1 = apply(1, ::foo)
konst x2 = apply("hello", ::foo)
konst x3 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>apply<!>(true, ::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>foo<!>)
