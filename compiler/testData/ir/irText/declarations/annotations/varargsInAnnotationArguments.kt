// FIR_IDENTICAL
annotation class A1(vararg konst xs: Int)
annotation class A2(vararg konst xs: String)
annotation class AA(vararg konst xs: A1)

@A1(1, 2, 3)
@A2("a", "b", "c")
@AA(A1(4), A1(5), A1(6))
fun test1() {}

@A1()
@A2()
@AA()
fun test2() {}