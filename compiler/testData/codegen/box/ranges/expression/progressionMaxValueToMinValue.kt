// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MaxI = Int.MAX_VALUE
konst MinI = Int.MIN_VALUE
konst MaxB = Byte.MAX_VALUE
konst MinB = Byte.MIN_VALUE
konst MaxS = Short.MAX_VALUE
konst MinS = Short.MIN_VALUE
konst MaxL = Long.MAX_VALUE
konst MinL = Long.MIN_VALUE
konst MaxC = Char.MAX_VALUE
konst MinC = Char.MIN_VALUE

fun box(): String {
    konst list1 = ArrayList<Int>()
    konst range1 = MaxI..MinI step 1
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>()) {
        return "Wrong elements for MaxI..MinI step 1: $list1"
    }

    konst list2 = ArrayList<Int>()
    konst range2 = MaxB..MinB step 1
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>()) {
        return "Wrong elements for MaxB..MinB step 1: $list2"
    }

    konst list3 = ArrayList<Int>()
    konst range3 = MaxS..MinS step 1
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>()) {
        return "Wrong elements for MaxS..MinS step 1: $list3"
    }

    konst list4 = ArrayList<Long>()
    konst range4 = MaxL..MinL step 1
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>()) {
        return "Wrong elements for MaxL..MinL step 1: $list4"
    }

    konst list5 = ArrayList<Char>()
    konst range5 = MaxC..MinC step 1
    for (i in range5) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>()) {
        return "Wrong elements for MaxC..MinC step 1: $list5"
    }

    return "OK"
}
