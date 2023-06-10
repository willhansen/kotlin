// FILE: list.kt

konst intList = listOf(1, 2, 3)
konst longList = listOf(1L, 2L, 3L)

// FILE: box.kt

fun box(): String {
    konst intListMin = intList.minByOrNull { it } ?: -1
    if (intListMin != 1) return "Fail intListMin=$intListMin"

    konst intListMax = intList.maxByOrNull { it } ?: -1
    if (intListMax != 3) return "Fail intListMax=$intListMax"

    konst longListMin = longList.minByOrNull { it } ?: -1
    if (longListMin != 1L) return "Fail longListMin=$longListMin"

    konst longListMax = longList.maxByOrNull { it } ?: -1
    if (longListMax != 3L) return "Fail longListMax=$longListMax"

    return "OK"
}

// @BoxKt.class:
// -- no compareTo
// 0 compareTo
// -- comparisons are properly fused with conditional jumps
// comparisons: 0 + fake inline variables: 12
// 12 ICONST_0
// 1 IF_ICMPGE
// 1 IF_ICMPLE
// 4 LCMP
// 1 IFGE
// 1 IFLE

// 0 konstueOf
// 0 Intrinsics.stringPlus
// 4 StringBuilder.<init>
// 8 StringBuilder.append
// 4 StringBuilder.toString
