// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<UInt>()
    konst range1 = (3u downTo 5u).reversed()
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<UInt>()) {
        return "Wrong elements for (3u downTo 5u).reversed(): $list1"
    }

    konst list2 = ArrayList<UInt>()
    konst range2 = (3u.toUByte() downTo 5u.toUByte()).reversed()
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<UInt>()) {
        return "Wrong elements for (3u.toUByte() downTo 5u.toUByte()).reversed(): $list2"
    }

    konst list3 = ArrayList<UInt>()
    konst range3 = (3u.toUShort() downTo 5u.toUShort()).reversed()
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<UInt>()) {
        return "Wrong elements for (3u.toUShort() downTo 5u.toUShort()).reversed(): $list3"
    }

    konst list4 = ArrayList<ULong>()
    konst range4 = (3uL downTo 5uL).reversed()
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<ULong>()) {
        return "Wrong elements for (3uL downTo 5uL).reversed(): $list4"
    }

    return "OK"
}
