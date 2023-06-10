// !LANGUAGE: +InlineClasses

interface A<T> {
    fun foo(a: T)
}

inline class Foo(konst x: Long) : A<Foo> {
    override fun foo(a: Foo) {}
}