// !DIAGNOSTICS: -UNUSED_PARAMETER

fun foo(vararg ii: Int) {}
fun foo(vararg ss: String) {}
fun foo(i: Int) {}

konst fn1: (Int) -> Unit = ::foo
konst fn2: (IntArray) -> Unit = ::foo
konst fn3: (Int, Int) -> Unit = ::foo
konst fn4: (Array<String>) -> Unit = ::foo
