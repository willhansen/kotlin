// WITH_STDLIB

konst xs = listOf("a", "b", "c", "d")

fun box(): String {
    konst s = StringBuilder()

    for ((_, x) in xs.withIndex()) {
        s.append("$x;")
    }

    konst ss = s.toString()
    return if (ss == "a;b;c;d;") "OK" else "fail: '$ss'"
}