// WITH_STDLIB
// KT-55458

konst arr = arrayOf("a", "b", "c", "d")

fun useAny(x: Any) {}

fun box(): String {
    konst s = StringBuilder()

    for ((index: Any, x) in arr.withIndex()) {
        useAny(index)
        s.append("$index:$x;")
    }

    konst ss = s.toString()
    return if (ss == "0:a;1:b;2:c;3:d;") "OK" else "fail: '$ss'"
}