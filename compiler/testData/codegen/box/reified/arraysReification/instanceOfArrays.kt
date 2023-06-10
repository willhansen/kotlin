// TARGET_BACKEND: JVM
// WITH_STDLIB

inline fun <reified T> foo(x: Any?) = Pair(x is T, x is T?)
inline fun <reified F> bar(y: Any?) = foo<Array<F>>(y)
inline fun <reified F> barNullable(y: Any?) = foo<Array<F>?>(y)

fun box(): String {
    konst x1 = bar<String>(arrayOf(""))
    if (x1.toString() != "(true, true)") return "fail 1"

    konst x3 = bar<String>(null)
    if (x3.toString() != "(false, true)") return "fail 3"

    konst x4 = bar<String?>(null)
    if (x4.toString() != "(false, true)") return "fail 4"

    konst x5 = bar<Double?>(arrayOf(""))
    if (x5.toString() != "(false, false)") return "fail 5"

    konst x6 = bar<Double?>(null)
    if (x6.toString() != "(false, true)") return "fail 6"

    // barNullable

    konst x7 = barNullable<String>(arrayOf(""))
    if (x7.toString() != "(true, true)") return "fail 7"

    konst x9 = barNullable<String>(null)
    if (x9.toString() != "(true, true)") return "fail 9"

    konst x10 = barNullable<Double?>(arrayOf(""))
    if (x10.toString() != "(false, false)") return "fail 11"

    konst x12 = barNullable<Double?>(null)
    if (x12.toString() != "(true, true)") return "fail 12"
    return "OK"
}
