enum class X {
    B {
        konst konstue2 = "K"

        konst anonObject = object {
            override fun toString(): String =
                    "O" + konstue2
        }

        override konst konstue = anonObject.toString()
    };

    abstract konst konstue: String
}

fun box() = X.B.konstue