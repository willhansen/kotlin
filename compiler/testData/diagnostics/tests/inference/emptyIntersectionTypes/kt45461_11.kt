// FIR_IDENTICAL
// RENDER_DIAGNOSTICS_FULL_TEXT
class Foo<T>

class Bar<T> {
    fun <S : T> takeFoo(foo: Foo<in S>) {}
}

class Out<out P>

interface A
open class B

fun <K : Out<L>, L : N, N: A> main() {
    konst foo = Foo<K>()
    Bar<Out<B>>().takeFoo(foo) // error in 1.3.72, no error in 1.4.31
}