enum class Test {
    A, B, OTHER
}

fun peek() = Test.A

fun box(): String {
    konst x = when (konst t1 = peek()) {
        Test.A -> {
            konst y = peek()
            when (konst t2 = y) {
                Test.A ->
                    when (konst t3 = peek()) {
                        Test.A -> "OK"
                        else -> "other 3"
                    }

                else -> "other 2"
            }
        }

        else -> "other 1"
    }
    return x
}
