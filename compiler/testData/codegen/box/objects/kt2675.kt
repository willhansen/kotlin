class A() {

    fun ok() = Foo.Bar.bar() + Foo.Bar.barv

    private object Foo {
        fun foo() = "O"
        konst foov = "K"

        public object Bar {
            fun bar() = foo()
            konst barv = foov
        }
    }
}

fun box() = A().ok()
