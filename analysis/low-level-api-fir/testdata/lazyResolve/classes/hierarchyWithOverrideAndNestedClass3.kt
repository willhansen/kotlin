interface Foo1 {
    fun foo()
    fun bar()
    konst str: String

    class ClassFromInterface
}

interface Foo2 : Foo1 {
    fun foo(i: Int)
    fun bar(s: String)
    konst isBoo: Boolean
}

interface Foo3 : Foo1 {
    fun foo(i: Int)
    fun bar(s: String)
    konst isBoo: Boolean
}

abstract class OuterClass : Foo1 {
    class Simple<caret>NestedClass {
        fun foo() {

        }
    }

    abstract class NestedClass : Foo2 {
        override fun foo() {}
    }

    abstract class AnotherNestedClass : NestedClass() {
        override fun bar(s: String) {

        }
    }
}
