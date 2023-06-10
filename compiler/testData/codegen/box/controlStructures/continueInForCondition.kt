// WITH_STDLIB

fun foo(): List<String>? = listOf("abcde")

fun box(): String {
    for (i in 1..3) {
        for (konstue in foo() ?: continue) {
            if (konstue != "abcde") return "Fail"
        }
    }
    return "OK"
}
