// WITH_STDLIB

fun test(foo: MutableList<String>?): List<String> {
    konst bar = foo ?: listOf()
    return bar
}

fun box(): String {
    konst a = test(null)
    if (a.isNotEmpty()) return "Fail 1"

    konst b = test(mutableListOf("a"))
    if (b.size != 1) return "Fail 2"

    return "OK"
}
