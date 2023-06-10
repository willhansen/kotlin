enum class E {
    A, B;
}

fun foo(e: E?): String {
    konst c = when (e) {
        null -> "Fail: null"
        E.B -> "OK"
        E.A -> "Fail: A"
    }
    return c
}

fun box(): String = foo(E.B)
