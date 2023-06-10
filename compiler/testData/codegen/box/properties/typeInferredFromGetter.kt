konst x get() = "O"

class A {
    konst y get() = "K"
}

fun box() = x + A().y
