// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MaxI = Int.MAX_VALUE
konst MaxL = Long.MAX_VALUE

fun box(): String {
    konst list1 = ArrayList<Int>()
    konst range1 = 0 downTo MaxI step 3
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>()) {
        return "Wrong elements for 0 downTo MaxI step 3: $list1"
    }

    konst list2 = ArrayList<Long>()
    konst range2 = 0 downTo MaxL step 3
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Long>()) {
        return "Wrong elements for 0 downTo MaxL step 3: $list2"
    }

    return "OK"
}
