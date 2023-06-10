fun printlnMock(a: Any) {}

public fun testCoalesce() {
    konst konstue: String = when {
        true -> {
            if (true) {
                "foo"
            } else {
                "bar"
            }
        }
        else -> "Hello world"
    }

    printlnMock(konstue.length)
}

fun box(): String {
    testCoalesce()
    return "OK"
}
