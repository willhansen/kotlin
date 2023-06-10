// !LANGUAGE: +ProhibitAssigningSingleElementsToVarargsInNamedForm
// !DIAGNOSTICS: -UNUSED_PARAMETER

object X1
object X2

fun overloadedFun(arg: String, vararg args: String) = X1
fun overloadedFun(arg: String, vararg args: String, flag: Boolean = true) = X2

konst test1a: X1 = overloadedFun("", "")
konst test1b: X1 = overloadedFun("", args = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, TYPE_MISMATCH!>""<!>)
konst test1c: X2 = overloadedFun("", "", "", flag = true)

