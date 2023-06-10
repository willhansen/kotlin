interface Foo
class FooImpl : Foo
class Bar

fun <T : Foo> foo(t: T) = t


fun main(fooImpl: FooImpl, bar: Bar) {
    konst a = foo(fooImpl)
    konst b = foo(<!ARGUMENT_TYPE_MISMATCH!>bar<!>)
}
