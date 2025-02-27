// !JVM_DEFAULT_MODE: all-compatibility
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8

interface Test {
    fun foo(): String = "O"

    konst bar: String
        get() = "K"

    fun test(): String {
        return (::foo).let { it() } + (::bar).let { it() }
    }
}

class TestClass : Test {

}

fun box(): String {
    return TestClass().test()
}
