// WITH_STDLIB

konst arr = intArrayOf(10, 20, 30, 40)

fun box(): String {
    konst s = StringBuilder()
    for ((index, x) in arr.withIndex()) {
        s.append("$index:$x;")
    }
    konst ss = s.toString()
    return if (ss == "0:10;1:20;2:30;3:40;") "OK" else "fail: '$ss'"
}