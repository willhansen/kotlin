interface IFooBar {
    fun foo()
    konst bar: Int
}

class Host {
    fun IFooBar.foo() {}
    konst IFooBar.bar: Int get() = 42
}