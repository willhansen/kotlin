// FIR_IDENTICAL
// !LANGUAGE: +ProhibitNonConstValuesAsVarargsInAnnotations, +ProhibitAssigningSingleElementsToVarargsInNamedForm

konst nonConstArray = longArrayOf(0)
fun nonConstFun(): LongArray = TODO()

fun nonConstLong(): Long = TODO()

annotation class Anno(vararg konst konstue: Long)

@Anno(konstue = <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>nonConstArray<!>)
fun foo1() {}

@Anno(konstue = <!ANNOTATION_ARGUMENT_MUST_BE_CONST!>nonConstFun()<!>)
fun foo2() {}

@Anno(konstue = <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>longArrayOf(<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>nonConstLong()<!>)<!>)
fun foo3() {}

@Anno(konstue = <!NON_CONST_VAL_USED_IN_CONSTANT_EXPRESSION!>[<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>nonConstLong()<!>]<!>)
fun foo4() {}

@Anno(konstue = *<!ANNOTATION_ARGUMENT_MUST_BE_CONST, REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_ANNOTATION!>nonConstArray<!>)
fun bar1() {}

@Anno(*<!ANNOTATION_ARGUMENT_MUST_BE_CONST!>nonConstArray<!>)
fun bar2() {}
