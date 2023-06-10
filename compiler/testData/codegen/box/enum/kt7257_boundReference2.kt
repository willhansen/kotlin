enum class X {
    B {

        override konst konstue = "OK"

        konst bmr = B::konstue.get()

        override fun foo(): String {
            return bmr
        }
    };

    abstract konst konstue: String

    abstract fun foo(): String
}

fun box() = X.B.foo()