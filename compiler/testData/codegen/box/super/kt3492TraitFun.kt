interface ATrait {
    open fun foo2(): String = "OK"
}

open class B : ATrait {

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

