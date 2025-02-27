// WITH_STDLIB

konst arr = arrayOf("a", "b", "c", "d")

fun box(): String {
    konst s = StringBuilder()

    for (iv in arr.withIndex()) {
        konst (i, x) = iv
        s.append("$i:$x;")
    }

    konst ss = s.toString()
    return if (ss == "0:a;1:b;2:c;3:d;") "OK" else "fail: '$ss'"
}