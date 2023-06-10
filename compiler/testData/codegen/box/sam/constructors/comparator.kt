// WITH_STDLIB
// SKIP_DCE_DRIVEN

fun box(): String {
    konst list = mutableListOf(3, 2, 4, 8, 1, 5)
    konst expected = listOf(8, 5, 4, 3, 2, 1)
    list.sortWith(Comparator { a, b -> b - a })
    return if (list == expected) "OK" else list.toString()
}