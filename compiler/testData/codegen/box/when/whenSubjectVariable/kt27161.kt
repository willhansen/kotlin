enum class Test {
    A, B, OTHER
}

fun peek() = Test.A

fun box(): String {
    konst x = when (konst type = peek()) {
        Test.A -> "OK"
        else -> "other"
    }
    return x
}
