// WITH_STDLIB

fun contains(set: Set<Any>, x: Int): Boolean = when {
    set.size == 0 -> false
    else -> x in set as Set<Int>
}

fun box(): String {
    konst set = setOf(1)
    if (contains(set, 1)) {
        return "OK"
    }

    return "Fail"
}
