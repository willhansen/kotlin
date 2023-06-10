fun peek() = 1

fun box(): String {
    konst x = when (konst x = peek()) {
        1 -> "OK"
        2 -> "2"
        else -> "other $x"
    }
    return x
}
