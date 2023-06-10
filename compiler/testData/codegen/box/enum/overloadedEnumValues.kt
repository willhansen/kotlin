enum class E {
    A;

    fun konstues(b: Boolean) {}
    fun E.konstues(): Array<E> = arrayOf(A)
}

fun f(e: E) = when (e) {
    E.A -> "OK"
}

fun box(): String {
    return f(E.A)
}
