// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo() {}
fun foo(s: String) {}

konst x1 = ::<!OVERLOAD_RESOLUTION_AMBIGUITY!>foo<!>
konst x2: () -> Unit = ::foo
konst x3: (String) -> Unit = ::foo
konst x4: (Int) -> Unit = ::<!NONE_APPLICABLE!>foo<!>