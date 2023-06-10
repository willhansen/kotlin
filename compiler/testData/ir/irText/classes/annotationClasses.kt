// FIR_IDENTICAL
annotation class Test1(konst x: Int)

annotation class Test2(konst x: Int = 0)

annotation class Test3(konst x: Test1)

annotation class Test4(vararg konst xs: Int)