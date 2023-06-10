interface IFooBar {
    fun foo()
    konst bar: Int
}

class Host {
    fun IFooBar.<!EXTENSION_SHADOWED_BY_MEMBER!>foo<!>() {}
    konst IFooBar.<!EXTENSION_SHADOWED_BY_MEMBER!>bar<!>: Int get() = 42
}