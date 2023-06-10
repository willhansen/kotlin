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

abstract class Usage : Foo2 {
    override fun fo<caret>o() {}
}
