// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    konst list1 = ArrayList<Int>()
    for (i in (8 downTo 3 step 2).reversed()) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>(4, 6, 8)) {
        return "Wrong elements for (8 downTo 3 step 2).reversed(): $list1"
    }

    konst list2 = ArrayList<Int>()
    for (i in (8.toByte() downTo 3.toByte() step 2).reversed()) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>(4, 6, 8)) {
        return "Wrong elements for (8.toByte() downTo 3.toByte() step 2).reversed(): $list2"
    }

    konst list3 = ArrayList<Int>()
    for (i in (8.toShort() downTo 3.toShort() step 2).reversed()) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>(4, 6, 8)) {
        return "Wrong elements for (8.toShort() downTo 3.toShort() step 2).reversed(): $list3"
    }

    konst list4 = ArrayList<Long>()
    for (i in (8L downTo 3L step 2L).reversed()) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>(4, 6, 8)) {
        return "Wrong elements for (8L downTo 3L step 2L).reversed(): $list4"
    }

    konst list5 = ArrayList<Char>()
    for (i in ('d' downTo 'a' step 2).reversed()) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>('b', 'd')) {
        return "Wrong elements for ('d' downTo 'a' step 2).reversed(): $list5"
    }

    return "OK"
}
