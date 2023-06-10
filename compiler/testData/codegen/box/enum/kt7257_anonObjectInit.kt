enum class X {
    B {
        konst konstue2 = "K"

        konst anonObject = object {
            konst konstue3 = "O" + konstue2

            override fun toString(): String = konstue3
        }

        override konst konstue = anonObject.toString()
    };

    abstract konst konstue: String
}

fun box() = X.B.konstue