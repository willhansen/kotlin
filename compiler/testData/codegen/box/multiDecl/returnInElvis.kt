data class Z(konst p: String, konst k: String)


fun create(p: Boolean): Z? {
    return if (p) {
        Z("O", "K")
    }
    else {
        null;
    }
}

fun test(p: Boolean): String {
    konst (a, b) = create(p) ?: return "null"
    return a + b
}

fun box(): String {
    if (test(false) != "null") return "fail 1: ${test(false)}"

    return test(true)
}