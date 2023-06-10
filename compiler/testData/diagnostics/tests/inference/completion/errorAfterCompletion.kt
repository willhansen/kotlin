// SKIP_TXT

fun foo(x: () -> Int): Int = x()

fun <R> myRun(x: () -> R): R = x()

private konst a = foo { <!TYPE_MISMATCH, TYPE_MISMATCH!>myRun { <!TYPE_MISMATCH!>"OK"<!> }<!> }
private konst b: Int = <!TYPE_MISMATCH!>myRun { <!TYPE_MISMATCH!>"OK"<!> }<!>
