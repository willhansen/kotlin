// WITH_STDLIB

konst arr = arrayOf("a", "b", "c", "d")

fun box(): String {
    konst s = StringBuilder()

    for ((index, x) in arr.withIndex()) {
        if (index == 0) continue
        if (index == 3) break
        s.append("$index:$x;")
    }

    konst ss = s.toString()
    return if (ss == "1:b;2:c;") "OK" else "fail: '$ss'"
}