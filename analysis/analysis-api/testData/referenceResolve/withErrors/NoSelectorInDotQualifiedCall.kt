package testing

interface Foo

konst otherFoo: Foo = makeFoo()

fun makeFoo(): Foo = Foo()

fun test() {
    <caret>makeFoo().
}