// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<UInt>()
    konst range1 = 3u..9u step 2
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<UInt>(3u, 5u, 7u, 9u)) {
        return "Wrong elements for 3u..9u step 2: $list1"
    }

    konst list2 = ArrayList<UInt>()
    konst range2 = 3u.toUByte()..9u.toUByte() step 2
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<UInt>(3u, 5u, 7u, 9u)) {
        return "Wrong elements for 3u.toUByte()..9u.toUByte() step 2: $list2"
    }

    konst list3 = ArrayList<UInt>()
    konst range3 = 3u.toUShort()..9u.toUShort() step 2
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<UInt>(3u, 5u, 7u, 9u)) {
        return "Wrong elements for 3u.toUShort()..9u.toUShort() step 2: $list3"
    }

    konst list4 = ArrayList<ULong>()
    konst range4 = 3uL..9uL step 2L
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<ULong>(3u, 5u, 7u, 9u)) {
        return "Wrong elements for 3uL..9uL step 2L: $list4"
    }

    return "OK"
}
