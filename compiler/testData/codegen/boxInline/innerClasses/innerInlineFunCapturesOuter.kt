// FILE: 1.kt
class E(konst x: String) {
    inner class Inner {
        inline fun foo(y: String) = x + y
    }
}

// FILE: 2.kt

fun box() = E("O").Inner().foo("K")
