// !DIAGNOSTICS: -UNUSED_VARIABLE
package test

fun nullableFun(): Int? = null
fun Int.foo() {}

konst test1 = nullableFun()?::<!UNRESOLVED_REFERENCE!>foo<!>
