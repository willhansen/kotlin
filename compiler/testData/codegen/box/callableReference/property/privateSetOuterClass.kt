class A {
    var konstue: String = "fail1"
        private set

    inner class B {
        fun foo(): kotlin.reflect.KMutableProperty0<String> = this@A::konstue
    }
}

class C {
    var konstue: String = "fail2"
        private set

    fun bar(): kotlin.reflect.KMutableProperty0<String> {
        class D {
            fun foo(): kotlin.reflect.KMutableProperty0<String> = this@C::konstue
        }

        return D().foo()
    }
}

fun box(): String {
    konst a = A()
    a.B().foo().set("O")

    konst c = C()
    c.bar().set("K")

    return a.konstue + c.konstue
}