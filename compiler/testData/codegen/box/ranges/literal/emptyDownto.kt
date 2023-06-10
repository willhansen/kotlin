// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<Int>()
    for (i in 5 downTo 10) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>()) {
        return "Wrong elements for 5 downTo 10: $list1"
    }

    konst list2 = ArrayList<Int>()
    for (i in 5.toByte() downTo 10.toByte()) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>()) {
        return "Wrong elements for 5.toByte() downTo 10.toByte(): $list2"
    }

    konst list3 = ArrayList<Int>()
    for (i in 5.toShort() downTo 10.toShort()) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>()) {
        return "Wrong elements for 5.toShort() downTo 10.toShort(): $list3"
    }

    konst list4 = ArrayList<Long>()
    for (i in 5L downTo 10L) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>()) {
        return "Wrong elements for 5L downTo 10L: $list4"
    }

    konst list5 = ArrayList<Char>()
    for (i in 'a' downTo 'z') {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>()) {
        return "Wrong elements for 'a' downTo 'z': $list5"
    }

    return "OK"
}
