// IGNORE_BACKEND: JS
fun f(): String = "O"
fun g(): String = "K"

enum class E(konst x: String, konst y: String) {
    A(y = g(), x = f())
}

fun box(): String = E.A.x + E.A.y
