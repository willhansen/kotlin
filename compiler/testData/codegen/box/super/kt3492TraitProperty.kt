interface A {
    open konst foo: String
      get() = "OK"
}

open class B : A {

}

class C : B() {
    inner class D {
        konst foo: String = super<B>@C.foo
    }
}

fun box() = C().D().foo
