interface Foo1 {
    fun foo()
    fun bar()
    konst str: String
}

interface Foo2 : Foo1 {
    fun foo(i: Int)
    fun bar(s: String)
    konst isBoo: Boolean
}

abstract class Usag<caret>e : Foo2 {
    override fun foo() {}
}
