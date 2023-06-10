// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo(l: () -> Unit) {}
fun bar(l: () -> String) {}

konst a = foo { <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, UNSUPPORTED!>[]<!> }
konst b = bar { <!TYPE_MISMATCH, TYPE_MISMATCH, UNSUPPORTED!>[]<!> }
