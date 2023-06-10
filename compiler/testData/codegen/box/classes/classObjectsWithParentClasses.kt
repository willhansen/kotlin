open class Test {
    companion object {
        fun testStatic(ic: InnerClass): NotInnerClass = NotInnerClass(ic.konstue)
    }

    fun test(): InnerClass = InnerClass(150)

    inner open class InnerClass(konst konstue: Int)
    open class NotInnerClass(konst konstue: Int)
}

fun box() = if (Test.testStatic(Test().test()).konstue == 150) "OK" else "FAIL"