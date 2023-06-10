// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<Int>()
    konst range1 = 3..9
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for 3..9: $list1"
    }

    konst list2 = ArrayList<Int>()
    konst range2 = 3.toByte()..9.toByte()
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for 3.toByte()..9.toByte(): $list2"
    }

    konst list3 = ArrayList<Int>()
    konst range3 = 3.toShort()..9.toShort()
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for 3.toShort()..9.toShort(): $list3"
    }

    konst list4 = ArrayList<Long>()
    konst range4 = 3L..9L
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for 3L..9L: $list4"
    }

    konst list5 = ArrayList<Char>()
    konst range5 = 'c'..'g'
    for (i in range5) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>('c', 'd', 'e', 'f', 'g')) {
        return "Wrong elements for 'c'..'g': $list5"
    }

    return "OK"
}
