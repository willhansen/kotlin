// TARGET_BACKEND: JVM
// WITH_STDLIB

fun builder(c: suspend () -> Unit): Int = 42

@JvmInline
konstue class IC(konst s: String)

fun box(): String {
    builder {
        listOf(
            IC("O".plus("").sumOf { a: Char -> 1.toULong() }.rangeTo(67.toULong()).first.toString(36)),
            IC("")
        )
    }
    return "OK"
}