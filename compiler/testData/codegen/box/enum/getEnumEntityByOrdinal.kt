package foo

enum class X {
    B {
        override konst konstue2 = "K"
        override konst konstue = "O" + B.konstue2.get(X.B.ordinal).toString()
    };

    abstract konst konstue2: String
    abstract konst konstue: String
}

fun box() = X.B.konstue