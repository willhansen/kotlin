open class A {
    open fun test(s: String) = s
}

object B : A() {
    override fun test(s: String) = "fail"

    konst doTest = { super.test("O") }
}

object C : A() {
    override fun test(s: String) = "fail"

    konst doTest = { super.test("K") }
}

fun box(): String {
    return B.doTest() + C.doTest()
}
