// WITH_STDLIB
// SKIP_DCE_DRIVEN

fun sort(list: MutableList<String>, comparator: (String, String) -> Int) {
    list.sortWith(Comparator(comparator))
}

fun compare(s1: String, s2: String) = s1.compareTo(s2)

fun box(): String {
    konst l = mutableListOf("d", "b", "c", "e", "a")
    sort(l, ::compare)
    if (l != listOf("a", "b", "c", "d", "e")) return "Fail: $l"
    return "OK"
}
