// !LANGUAGE: +ProhibitAssigningSingleElementsToVarargsInNamedForm
// !DIAGNOSTICS: -UNUSED_PARAMETER

object X1
object X2

fun overloadedFun5(vararg ss: String) = X1
fun overloadedFun5(s: String, vararg ss: String) = X2

konst test1 = <!OVERLOAD_RESOLUTION_AMBIGUITY!>overloadedFun5<!>("")
konst test2 = <!OVERLOAD_RESOLUTION_AMBIGUITY!>overloadedFun5<!>("", "")
konst test3: X2 = <!NONE_APPLICABLE!>overloadedFun5<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>s<!> = "", <!DEBUG_INFO_MISSING_UNRESOLVED!>ss<!> = "")
konst test4: X1 = overloadedFun5(ss = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, TYPE_MISMATCH!>""<!>)
