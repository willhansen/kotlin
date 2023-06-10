// FIR_IDENTICAL
@DslMarker
annotation class MyDsl

@MyDsl
interface Foo<T> {
    konst x: Int
}

konst Foo<*>.bad: Int get() = x

fun Foo<*>.badFun(): Int = x

konst Foo<Int>.good: Int get() = x

fun test(foo: Foo<*>) {
    foo.apply {
        x
    }
}