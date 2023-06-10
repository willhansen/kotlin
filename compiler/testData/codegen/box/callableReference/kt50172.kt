// WITH_STDLIB

fun g(s: String): List<String> {
    fun f(x: String): String = x + "K"

    konst f = f(s)
    return listOf(s).map(::f)
}

fun box(): String =
    g("O").first()
