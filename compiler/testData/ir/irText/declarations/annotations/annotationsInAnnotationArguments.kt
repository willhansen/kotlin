// FIR_IDENTICAL
annotation class A1(konst x: Int)
annotation class A2(konst a: A1)
annotation class AA(konst xs: Array<A1>)

@A2(A1(42))
@AA([A1(1), A1(2)])
fun test() {}
