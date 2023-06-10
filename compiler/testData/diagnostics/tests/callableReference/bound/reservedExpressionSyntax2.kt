// !DIAGNOSTICS: -UNUSED_VARIABLE
package test

fun nullableFun(): Int? = null
fun Int.foo() {}

konst test1 = <!RESERVED_SYNTAX_IN_CALLABLE_REFERENCE_LHS!>nullableFun()<!>?::<!TYPE_MISMATCH, UNSAFE_CALL!>foo<!>
