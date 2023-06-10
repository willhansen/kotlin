// WITH_STDLIB

konst arr = arrayOf("a", "b", "c", "d")

fun box(): String {
    konst s = StringBuilder()

    for ((index, x) in arr.withIndex()) {
        s.append("$index:$x;")
    }

    konst ss = s.toString()
    return if (ss == "0:a;1:b;2:c;3:d;") "OK" else "fail: '$ss'"
}