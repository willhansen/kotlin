// ISSUE: KT-49045

data class Foo(konst foo: String)
class Bar(konst bar: String)

fun Bar.toFoo() = Foo(bar)

class Wrapper<T>(konst wrapped: T) {
    fun <A> map(f: (T) -> A): Wrapper<A> = Wrapper(f(wrapped))
    fun swapWrappedValue(f: (T) -> Wrapper<T>): Wrapper<T> = f(wrapped)
}

fun test_1(): Wrapper<Foo?> {
    return Wrapper(Bar("bar"))
        .map { it.toFoo() as Foo? }
        .swapWrappedValue { Wrapper(null) }
}

fun test_2(): Wrapper<Foo?> {
    return <!RETURN_TYPE_MISMATCH!>Wrapper(Bar("bar"))
        .map { it.toFoo() }
        .swapWrappedValue { <!TYPE_MISMATCH, TYPE_MISMATCH!>Wrapper(null)<!> }<!>
}
