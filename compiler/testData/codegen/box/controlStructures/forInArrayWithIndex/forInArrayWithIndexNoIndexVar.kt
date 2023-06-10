// WITH_STDLIB

konst arr = arrayOf("a", "b", "c", "d")

fun box(): String {
    konst s = StringBuilder()

    for ((_, x) in arr.withIndex()) {
        s.append("$x;")
    }

    konst ss = s.toString()
    return if (ss == "a;b;c;d;") "OK" else "fail: '$ss'"
}