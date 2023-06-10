fun <T> ekonst(fn: () -> T) = fn()

fun String.f(x: String): String {
    fun String.g() = ekonst { this@f + this@g }
    return x.g()
}

fun box() = "O".f("K")
