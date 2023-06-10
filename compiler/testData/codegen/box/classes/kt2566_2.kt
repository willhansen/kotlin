open class A {
    open konst foo: String = "OK"
}

open class B : A() {
    inner class E {
        konst foo: String = super<A>@B.foo
    }
}

class C : B() {
    inner class D {
        konst foo: String = super<B>@C.foo
    }
}

fun box() = C().foo
