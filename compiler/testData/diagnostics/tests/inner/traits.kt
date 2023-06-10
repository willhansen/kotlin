// FIR_IDENTICAL
// KT-1188

interface Foo {
    konst b : Bar
    konst b1 : Foo.Bar
    interface Bar {}
}

interface Foo2 : Foo {
    konst bb1 : Foo.Bar
}
