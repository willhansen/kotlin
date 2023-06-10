// FIR_IDENTICAL
annotation class TestAnn(konst x: Int)

class TestClass @TestAnn(1) constructor() {
    @TestAnn(2) constructor(x: Int) : this()
}