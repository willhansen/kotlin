interface A {
    konst foo: (Int)->Int
}

fun test(a: A) {
    a.<caret>foo(1)
}