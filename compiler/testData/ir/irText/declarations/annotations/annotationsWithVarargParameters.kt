// FIR_IDENTICAL
annotation class A(vararg konst xs: String)

@A("abc", "def") fun test1() {}
@A("abc") fun test2() {}
@A fun test3() {}