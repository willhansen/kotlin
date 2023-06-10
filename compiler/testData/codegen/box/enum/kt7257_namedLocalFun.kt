enum class X {
    B {
        konst konstue2 = "K"

        konst konstue3: String
        init {
            fun foo() = konstue2
            konstue3 = "O" + foo()
        }

        override konst konstue = konstue3
    };

    abstract konst konstue: String
}

fun box() = X.B.konstue