// SKIP_TXT

fun foo(x: () -> Int): Int = x()

fun <R> myRun(x: () -> R): R = x()

private konst a = foo { myRun { <!ARGUMENT_TYPE_MISMATCH!>"OK"<!> } }
private konst b: Int = myRun { <!ARGUMENT_TYPE_MISMATCH!>"OK"<!> }
