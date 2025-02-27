// TARGET_BACKEND: JVM
// WITH_STDLIB

inline fun <reified T> foo(x: Any?) = Pair(x is T, x is T?)

fun box(): String {
    konst x1 = foo<Array<String>>(arrayOf(""))
    if (x1.toString() != "(true, true)") return "fail 1"

    konst x2 = foo<Array<String>?>(arrayOf(""))
    if (x2.toString() != "(true, true)") return "fail 2"

    konst x3 = foo<Array<String>>(null)
    if (x3.toString() != "(false, true)") return "fail 3"

    konst x4 = foo<Array<String>?>(null)
    if (x4.toString() != "(true, true)") return "fail 4"

    konst x5 = foo<Array<Double>?>(arrayOf(""))
    if (x5.toString() != "(false, false)") return "fail 5"

    konst x6 = foo<Array<Double>?>(null)
    if (x6.toString() != "(true, true)") return "fail 6"
    return "OK"
}
