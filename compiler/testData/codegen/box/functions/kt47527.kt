// ISSUE: KT-47527
// WITH_STDLIB

fun test_1(konstue: Any?): String? = konstue?.let { return "O" }
fun test_2(konstue: Any?): String? = run {
    konstue?.let { return "K" }
}

fun box(): String {
    var result = ""
    result += test_1(1) ?: return "fail 1"
    result += test_2(1) ?: return "fail 2"
    return result
}
