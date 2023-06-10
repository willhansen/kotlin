// FIR_IDENTICAL
fun foo(init: A.() -> Unit) { }

class A {
    var x: Int = 0
    companion object {
        konst f = foo() {
            x = 1
        }
    }
}

class B(konst a: String) {
    fun f() = 0
    companion object   {
        fun B.bar() = a + f()
    }
}

open class C {
    fun bar() {}
}

class E: C() {

    class D {
        init {
            with(C()) {
                bar()
                this.bar()
            }
        }
    }
}
