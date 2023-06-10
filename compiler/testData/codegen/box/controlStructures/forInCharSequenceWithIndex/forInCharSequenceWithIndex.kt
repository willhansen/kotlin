// WITH_STDLIB

konst cs: CharSequence = "abcd"

fun box(): String {
    konst s = StringBuilder()

    for ((index, x) in cs.withIndex()) {
        s.append("$index:$x;")
    }

    konst ss = s.toString()
    return if (ss == "0:a;1:b;2:c;3:d;") "OK" else "fail: '$ss'"
}