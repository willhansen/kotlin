// !LANGUAGE: +InlineClasses

interface A<T> {
    fun foo(): T
}

inline class Foo(konst x: Long) : A<Foo> {
    override fun foo() = this
}