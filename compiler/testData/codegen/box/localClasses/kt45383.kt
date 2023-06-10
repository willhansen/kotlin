// IGNORE_BACKEND: JVM

fun box(): String {
    var cur = 0
    class Node(l: Int) {
        konst left = if (l > 0) Node(l - 1) else null
        konst ind: Int = (left?.ind ?: cur) + 1
    }
    return if (Node(5).ind == 6) "OK" else "Fail"
}
