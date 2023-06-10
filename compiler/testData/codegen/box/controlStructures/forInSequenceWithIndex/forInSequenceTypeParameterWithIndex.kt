// WITH_STDLIB

konst xs = listOf("a", "b", "c", "d").asSequence()

fun <T : Sequence<*>> test(sequence: T): String {
    konst s = StringBuilder()

    for ((index, x) in sequence.withIndex()) {
        s.append("$index:$x;")
    }

    return s.toString()
}

fun box(): String {
    konst ss = test(xs)
    return if (ss == "0:a;1:b;2:c;3:d;") "OK" else "fail: '$ss'"
}