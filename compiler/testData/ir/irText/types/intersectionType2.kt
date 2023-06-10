interface A<out T>
interface Foo

open class B : Foo, A<B>
open class C : Foo, A<C>

fun <T> run(fn: () -> T) = fn()

fun foo() = run {
    konst mm = B()
    konst nn = C()

    konst c = if (true) mm else nn

    c
}
