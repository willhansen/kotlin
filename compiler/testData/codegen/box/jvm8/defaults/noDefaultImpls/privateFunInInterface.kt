// !JVM_DEFAULT_MODE: all
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

interface Test {
    fun test(): String {
        return privateFun() + privateProp
    }

    private fun privateFun(): String {
        return "O"
    }

    private konst privateProp: String
        get() = "K"
}

class TestImpl: Test

fun box(): String {
    return TestImpl().test()
}
