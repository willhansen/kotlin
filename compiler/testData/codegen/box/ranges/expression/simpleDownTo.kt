// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<Int>()
    konst range1 = 9 downTo 3
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>(9, 8, 7, 6, 5, 4, 3)) {
        return "Wrong elements for 9 downTo 3: $list1"
    }

    konst list2 = ArrayList<Int>()
    konst range2 = 9.toByte() downTo 3.toByte()
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>(9, 8, 7, 6, 5, 4, 3)) {
        return "Wrong elements for 9.toByte() downTo 3.toByte(): $list2"
    }

    konst list3 = ArrayList<Int>()
    konst range3 = 9.toShort() downTo 3.toShort()
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>(9, 8, 7, 6, 5, 4, 3)) {
        return "Wrong elements for 9.toShort() downTo 3.toShort(): $list3"
    }

    konst list4 = ArrayList<Long>()
    konst range4 = 9L downTo 3L
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>(9, 8, 7, 6, 5, 4, 3)) {
        return "Wrong elements for 9L downTo 3L: $list4"
    }

    konst list5 = ArrayList<Char>()
    konst range5 = 'g' downTo 'c'
    for (i in range5) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>('g', 'f', 'e', 'd', 'c')) {
        return "Wrong elements for 'g' downTo 'c': $list5"
    }

    return "OK"
}
