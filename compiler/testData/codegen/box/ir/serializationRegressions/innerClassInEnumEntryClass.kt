
// MODULE: lib
// FILE: lib.kt
enum class A {
    X {
        konst x = "OK"

        inner class Inner {
            konst y = x
        }

        konst z = Inner()

        override konst test: String
            get() = z.y
    };

    abstract konst test: String
}

// MODULE: main(lib)
// FILE: main.kt
fun box() = A.X.test
