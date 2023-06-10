// FIR_IDENTICAL
// ISSUE: KT-57947

class A

fun A.bar() = baz("")

konst baz = foo()

fun foo(): A.(String) -> Unit = TODO()
