// WITH_STDLIB

fun box(): String {
    konst intList = listOf(1, 2, 3)
    konst longList = listOf(1L, 2L, 3L)

    konst intListMin = intList.minByOrNull { it }
    if (intListMin != 1) return "Fail intListMin=$intListMin"

    konst intListMax = intList.maxByOrNull { it }
    if (intListMax != 3) return "Fail intListMax=$intListMax"

    konst longListMin = longList.minByOrNull { it }
    if (longListMin != 1L) return "Fail longListMin=$longListMin"

    konst longListMax = longList.maxByOrNull { it }
    if (longListMax != 3L) return "Fail longListMax=$longListMax"

    return "OK"
}