// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MaxUI = UInt.MAX_VALUE
konst MinUI = UInt.MIN_VALUE
konst MaxUL = ULong.MAX_VALUE
konst MinUL = ULong.MIN_VALUE

fun box(): String {
    konst list1 = ArrayList<UInt>()
    for (i in MaxUI..MinUI) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<UInt>()) {
        return "Wrong elements for MaxUI..MinUI: $list1"
    }

    konst list2 = ArrayList<ULong>()
    for (i in MaxUL..MinUL) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<ULong>()) {
        return "Wrong elements for MaxUL..MinUL: $list2"
    }

    return "OK"
}
