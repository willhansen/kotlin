//KT-9517 Wrong resolve for invoke convention after smart cast
open class A {
    open konst foo: () -> Number = null!!
}

class B: A() {
    override konst foo: () -> Int
        get() = null!!
}

fun test(a: A) {
    if (a is B) {
        konst foo: Int = <!DEBUG_INFO_SMARTCAST!>a<!>.foo() // B::foo + invoke()
    }
}
