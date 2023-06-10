// WITH_STDLIB

konst arr = intArrayOf(10, 20, 30, 40)

fun foo(xs: Any): String {
    if (xs !is IntArray) return "not an IntArray"

    konst s = StringBuilder()
    for ((index, x) in xs.withIndex()) {
        s.append("$index:$x;")
    }
    return s.toString()
}

fun box(): String {
    konst ss = foo(arr)
    return if (ss == "0:10;1:20;2:30;3:40;") "OK" else "fail: '$ss'"
}