
enum class X {
    B {
        konst k = "K"

        inner class Inner {
            fun foo() = "O" + k
        }

        konst inner = Inner()

        konst bmr = inner::foo

        override konst konstue = bmr.invoke()
    };

    abstract konst konstue: String
}

fun box() = X.B.konstue
