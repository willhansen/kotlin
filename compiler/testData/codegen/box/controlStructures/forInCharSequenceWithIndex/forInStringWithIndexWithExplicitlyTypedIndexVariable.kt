// WITH_STDLIB
// KT-55458

konst xs = "abcd"

fun useAny(x: Any) {}

fun box(): String {
    konst s = StringBuilder()

    for ((index: Any, x) in xs.withIndex()) {
        useAny(index)
        s.append("$index:$x;")
    }

    konst ss = s.toString()
    return if (ss == "0:a;1:b;2:c;3:d;") "OK" else "fail: '$ss'"
}