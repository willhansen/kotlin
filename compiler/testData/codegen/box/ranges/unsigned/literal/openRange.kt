// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<UInt>()
    for (i in 1u until 5u) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<UInt>(1u, 2u, 3u, 4u)) {
        return "Wrong elements for 1u until 5u: $list1"
    }

    konst list2 = ArrayList<UInt>()
    for (i in 1u.toUByte() until 5u.toUByte()) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<UInt>(1u, 2u, 3u, 4u)) {
        return "Wrong elements for 1u.toUByte() until 5u.toUByte(): $list2"
    }

    konst list3 = ArrayList<UInt>()
    for (i in 1u.toUShort() until 5u.toUShort()) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<UInt>(1u, 2u, 3u, 4u)) {
        return "Wrong elements for 1u.toUShort() until 5u.toUShort(): $list3"
    }

    konst list4 = ArrayList<ULong>()
    for (i in 1uL until 5uL) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<ULong>(1u, 2u, 3u, 4u)) {
        return "Wrong elements for 1uL until 5uL: $list4"
    }

    return "OK"
}
