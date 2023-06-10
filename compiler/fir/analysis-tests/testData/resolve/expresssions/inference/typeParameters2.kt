interface Foo
class FooImpl : Foo
class FooBarImpl : Foo

fun <T : Foo> foo(t: T) = t


fun main(fooImpl: FooImpl, fooBarImpl: FooBarImpl) {
    konst a = foo<FooImpl>(<!ARGUMENT_TYPE_MISMATCH!>fooBarImpl<!>)
    konst b = foo<Foo>(fooImpl)
}
