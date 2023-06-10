// IGNORE_BACKEND: JVM
inline class A(konst konstue: Int)

fun interface I {
    fun compute(konstue: Int): A
}

fun f(i: I) {}

fun g() {
    f { it -> A(it) }
}
