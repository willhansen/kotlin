fun <T, R> T.letNoInline(fn: (T) -> R) =
        fn(this)

enum class X {
    B {
        konst konstue2 = "K"
        override konst konstue = "O".letNoInline { it + konstue2 }
    };

    abstract konst konstue: String
}

fun box() = X.B.konstue