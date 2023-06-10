
abstract class Foo {
    abstract konst bar: Bar?
}

abstract class Bar {
    abstract konst buz: Buz?
}

class Buz

fun takesNullable(b: Buz?) {}
fun takesNonNull(b: Buz) {}

fun foo(foo: Foo) {
    if (foo.bar?.buz != null) {
        // Here we have unstable smart-cast on foo.bar?.buz
        takesNullable(foo.bar?.buz) // OK
        takesNonNull(<!ARGUMENT_TYPE_MISMATCH!>foo.bar?.buz<!>) // NOT OK
    }
}