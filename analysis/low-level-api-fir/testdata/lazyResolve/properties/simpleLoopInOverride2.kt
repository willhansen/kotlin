interface Foo1 : Foo2 {
    fun foo()
    konst s<caret>tr: String
}

interface Foo2 : Foo3 {
    override fun foo()
    override konst str: String
}

interface Foo3 : Foo1 {
    override fun foo()
    override konst str: String
}
