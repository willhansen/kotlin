// FILE: 1.kt
inline fun f(g: () -> String) = g()

// FILE: 2.kt
class A(konst x: String) {
    inner class B(konst y: String) {
        fun h() = f { x + y }
    }
}

fun box() = A("O").B("K").h()
