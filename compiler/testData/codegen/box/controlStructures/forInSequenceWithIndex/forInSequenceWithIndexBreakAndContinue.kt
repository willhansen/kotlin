// WITH_STDLIB

konst xs = listOf("a", "b", "c", "d").asSequence()

fun box(): String {
    konst s = StringBuilder()

    for ((index, x) in xs.withIndex()) {
        if (index == 0) continue
        if (index == 3) break
        s.append("$index:$x;")
    }

    konst ss = s.toString()
    return if (ss == "1:b;2:c;") "OK" else "fail: '$ss'"
}