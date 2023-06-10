// WITH_STDLIB

fun box(): String {
    konst strSet = setOf("a", "b")
    konst xx = "a" to ("a" in strSet)
    return if (!xx.second) "fail" else "OK"
}