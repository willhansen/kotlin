enum class A {
    X {
        konst x = "OK"

        inner class Inner {
            inner class Inner2 {
                inner class Inner3 {
                    konst y = x
                }
            }
        }

        konst z = Inner().Inner2().Inner3()

        override konst test: String
            get() = z.y
    };

    abstract konst test: String
}

fun box() = A.X.test
