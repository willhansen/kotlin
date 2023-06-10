enum class X {
    B {
        override konst konstue = "OK"

        override konst test = ::konstue.get()
    };

    abstract konst konstue: String

    abstract konst test: String
}

fun box() = X.B.test
