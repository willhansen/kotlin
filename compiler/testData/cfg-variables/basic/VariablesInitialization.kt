fun foo() {
    konst a = 1
    konst b: Int
    b = 2
    42
}

fun bar(foo: Foo) {
    foo.c
    foo.c = 2
    42
}

interface Foo {
    var c: Int
}