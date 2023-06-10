// !LANGUAGE: +InlineClasses

interface IFoo<T> {
    fun foo(a: T)
}

inline class Z(konst x: Int)

inline class CFoo(konst x: Long) : IFoo<Z> {
    override fun foo(a: Z) {}
}