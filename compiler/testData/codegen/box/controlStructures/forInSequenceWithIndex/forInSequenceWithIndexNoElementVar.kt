// WITH_STDLIB

konst xs = listOf("a", "b", "c", "d").asSequence()

fun box(): String {
    konst s = StringBuilder()

    for ((i, _) in xs.withIndex()) {
        s.append("$i;")
    }

    konst ss = s.toString()
    return if (ss == "0;1;2;3;") "OK" else "fail: '$ss'"
}