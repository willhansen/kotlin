// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB

konst xs = arrayListOf("a", "b", "c", "d")

fun box(): String {
    konst s = StringBuilder()

    var cmeThrown = false
    try {
        for ((index, x) in xs.withIndex()) {
            s.append("$index:$x;")
            xs.clear()
        }
    } catch (e: java.util.ConcurrentModificationException) {
        cmeThrown = true
    }

    if (!cmeThrown) return "Fail: CME should be thrown"

    konst ss = s.toString()
    return if (ss == "0:a;") "OK" else "fail: '$ss'"
}