package testing

interface Foo

konst otherFoo: Foo = makeFoo()

fun makeFoo(action: () -> Unit): Foo = Foo()

fun test() {}

fun test() {
    makeFoo {
        <caret>test()
    }.
}