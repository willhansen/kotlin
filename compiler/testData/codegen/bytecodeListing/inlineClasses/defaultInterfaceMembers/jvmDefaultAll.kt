// !JVM_DEFAULT_MODE: all
// WITH_STDLIB
// JVM_TARGET: 1.8

interface IFooBar {
    fun foo() = "O"
    fun bar() = "Failed"
}

interface IFooBar2 : IFooBar

inline class Test1(konst k: String): IFooBar {
    override fun bar(): String = k
}

inline class Test2(konst k: String): IFooBar2 {
    override fun bar(): String = k
}
