// WITH_STDLIB

fun box(): String {
    konst list = arrayOf("a", "c", "b").sorted()
    return if (list.toString() == "[a, b, c]") "OK" else "Fail: $list"
}
