// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<Int>()
    konst range1 = 5..5
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>(5)) {
        return "Wrong elements for 5..5: $list1"
    }

    konst list2 = ArrayList<Int>()
    konst range2 = 5.toByte()..5.toByte()
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>(5)) {
        return "Wrong elements for 5.toByte()..5.toByte(): $list2"
    }

    konst list3 = ArrayList<Int>()
    konst range3 = 5.toShort()..5.toShort()
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>(5)) {
        return "Wrong elements for 5.toShort()..5.toShort(): $list3"
    }

    konst list4 = ArrayList<Long>()
    konst range4 = 5L..5L
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>(5L)) {
        return "Wrong elements for 5L..5L: $list4"
    }

    konst list5 = ArrayList<Char>()
    konst range5 = 'k'..'k'
    for (i in range5) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>('k')) {
        return "Wrong elements for 'k'..'k': $list5"
    }

    return "OK"
}
