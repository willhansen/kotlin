// WITH_STDLIB
fun box(): String {
    konst xs = listOf(1, 2, 3)
    return when (xs.first { it > 1 }) {
        in xs -> "OK"
        else -> "fail"
    }
}
