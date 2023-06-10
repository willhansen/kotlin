open class A {
    open fun foo2(): String = "OK"
}

open class B : A() {

}

class C : B() {
    inner class D {
        konst foo: String = super<B>@C.foo2()
    }
}

fun box() : String {
    konst obj = C().D();
    return obj.foo
}

