interface Test {
    fun test(): String
}

open class Base(konst test: Test)

open class Outer(konst x: String) {
    open inner class Inner

    inner class JavacBug : Base(
            object : Outer.Inner(), Test {
                override fun test() = x
            }
    )
}

fun box() = Outer("OK").JavacBug().test.test()
