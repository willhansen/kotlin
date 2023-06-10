// !CHECK_TYPE

interface A {
    konst foo: Any?
}

interface C: A {
    override konst foo: String
}
interface B: A {
    override var foo: String?
}

fun test(a: A) {
    if (a is B && a is C) {
        a.foo = ""
        a.foo = null
        a.foo
    }
}
