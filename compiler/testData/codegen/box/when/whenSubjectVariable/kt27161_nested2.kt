enum class Test {
    A, B, OTHER
}

fun peek() = Test.A

fun box(): String {
    konst x = when (konst t1 = peek()) {
        Test.A -> {
            when (
                konst t2 = when(konst y = peek()) {
                    Test.A -> Test.A
                    Test.B -> Test.B
                    else -> Test.OTHER
                }
            ) {
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
