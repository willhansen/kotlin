// FIR_IDENTICAL
annotation class TestAnn(konst x: String)

fun testFun(@TestAnn("testFun.x") x: Int) {}

class TestClassConstructor1(@TestAnn("TestClassConstructor1.x")x: Int) {
    konst xx = x
}