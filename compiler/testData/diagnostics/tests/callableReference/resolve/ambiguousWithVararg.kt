// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

fun fun1() {}
fun fun1(x: Int) {}

konst ref1 = ::<!OVERLOAD_RESOLUTION_AMBIGUITY!>fun1<!>

fun fun2(vararg x: Int) {}
fun fun2(x: Int) {}

konst ref2 = ::<!OVERLOAD_RESOLUTION_AMBIGUITY!>fun2<!>

fun fun3(x0: Int, vararg xs: Int) {}
fun fun3(x0: String, vararg xs: String) {}

konst ref3 = ::<!OVERLOAD_RESOLUTION_AMBIGUITY!>fun3<!>
