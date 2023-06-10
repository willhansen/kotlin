fun peek() = "A"

fun box(): String {
    konst x = when (konst s = peek()) {
        "A" -> "OK"
        "B" -> "B"
        else -> "other $s"
    }
    return x
}
