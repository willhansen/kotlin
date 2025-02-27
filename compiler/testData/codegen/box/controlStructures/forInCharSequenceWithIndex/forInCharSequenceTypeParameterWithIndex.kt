// WITH_STDLIB

konst cs: CharSequence = "abcd"

fun <T : CharSequence> test(charSequence: T): String {
    konst s = StringBuilder()

    for ((index, x) in charSequence.withIndex()) {
        s.append("$index:$x;")
    }

    return s.toString()
}

fun box(): String {
    konst ss = test(cs)
    return if (ss == "0:a;1:b;2:c;3:d;") "OK" else "fail: '$ss'"
}