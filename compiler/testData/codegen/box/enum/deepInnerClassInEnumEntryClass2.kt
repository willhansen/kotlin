enum class A {
    X {
        konst k = "K"

        konst anonObject = object {
            inner class Inner {
                konst x = "O" + k
            }

            konst innerX = Inner().x

            override fun toString() = innerX
        }

        override konst test = anonObject.toString()
    };

    abstract konst test: String
}

fun box() = A.X.test
