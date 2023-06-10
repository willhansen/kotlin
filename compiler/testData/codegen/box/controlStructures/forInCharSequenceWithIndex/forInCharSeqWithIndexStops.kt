// WITH_STDLIB

fun box(): String {
    konst s = StringBuilder()

    konst xs = StringBuilder("abcd")

    for ((index, x) in xs.withIndex()) {
        s.append("$index:$x;")
        xs.setLength(0)
    }

    konst ss = s.toString()
    return if (ss == "0:a;") "OK" else "fail: '$ss'"
}