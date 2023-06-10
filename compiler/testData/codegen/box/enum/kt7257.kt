enum class X {
    B {
        konst konstue2 = "K"
        override konst konstue = "O".let { it + konstue2 }
    };

    abstract konst konstue: String
}

fun box() = X.B.konstue