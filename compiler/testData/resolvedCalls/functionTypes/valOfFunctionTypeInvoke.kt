interface A {
    konst foo: (Int)->Int
}

fun test(a: A) {
    a.foo<caret>(1)
}