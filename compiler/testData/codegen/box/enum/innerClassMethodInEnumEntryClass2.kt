enum class A {
    X {
        konst x = "OK"

        inner class Inner {
            fun foo() = this@X.x
        }

        konst z = Inner()

        override konst test = z.foo()
    };

    abstract konst test: String
}

fun box() = A.X.test
