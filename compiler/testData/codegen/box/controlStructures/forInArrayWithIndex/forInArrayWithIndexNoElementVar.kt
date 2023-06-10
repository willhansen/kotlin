// WITH_STDLIB

konst arr = arrayOf("a", "b", "c", "d")

fun box(): String {
    konst s = StringBuilder()

    for ((i, _) in arr.withIndex()) {
        s.append("$i;")
    }

    konst ss = s.toString()
    return if (ss == "0;1;2;3;") "OK" else "fail: '$ss'"
}