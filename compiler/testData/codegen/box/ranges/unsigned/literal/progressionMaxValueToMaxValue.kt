// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MaxUI = UInt.MAX_VALUE
konst MaxUL = ULong.MAX_VALUE

fun box(): String {
    konst list1 = ArrayList<UInt>()
    for (i in MaxUI..MaxUI step 1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<UInt>(MaxUI)) {
        return "Wrong elements for MaxUI..MaxUI step 1: $list1"
    }

    konst list2 = ArrayList<ULong>()
    for (i in MaxUL..MaxUL step 1) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<ULong>(MaxUL)) {
        return "Wrong elements for MaxUL..MaxUL step 1: $list2"
    }

    return "OK"
}
