// TARGET_BACKEND: JVM
// WITH_STDLIB

@JvmInline
konstue class IC(konst s: String)

fun asAny(a: Any) = a

fun box(): String {
    konst t = asAny(
        IC("O".plus("").sumOf { a: Char -> 1.toULong() }.rangeTo(67.toULong()).first.toString(36))
    ).toString()
    if (t != "IC(s=1)")
        return "Failed: t=$t"
    return "OK"
}
