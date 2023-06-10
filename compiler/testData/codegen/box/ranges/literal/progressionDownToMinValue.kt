// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MinI = Int.MIN_VALUE
konst MinB = Byte.MIN_VALUE
konst MinS = Short.MIN_VALUE
konst MinL = Long.MIN_VALUE
konst MinC = Char.MIN_VALUE

fun box(): String {
    konst list1 = ArrayList<Int>()
    for (i in (MinI + 2) downTo MinI step 1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>(MinI + 2, MinI + 1, MinI)) {
        return "Wrong elements for (MinI + 2) downTo MinI step 1: $list1"
    }

    konst list2 = ArrayList<Int>()
    for (i in (MinB + 2).toByte() downTo MinB step 1) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>((MinB + 2).toInt(), (MinB + 1).toInt(), MinB.toInt())) {
        return "Wrong elements for (MinB + 2).toByte() downTo MinB step 1: $list2"
    }

    konst list3 = ArrayList<Int>()
    for (i in (MinS + 2).toShort() downTo MinS step 1) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>((MinS + 2).toInt(), (MinS + 1).toInt(), MinS.toInt())) {
        return "Wrong elements for (MinS + 2).toShort() downTo MinS step 1: $list3"
    }

    konst list4 = ArrayList<Long>()
    for (i in (MinL + 2).toLong() downTo MinL step 1) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>((MinL + 2).toLong(), (MinL + 1).toLong(), MinL)) {
        return "Wrong elements for (MinL + 2).toLong() downTo MinL step 1: $list4"
    }

    konst list5 = ArrayList<Char>()
    for (i in (MinC + 2) downTo MinC step 1) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>((MinC + 2), (MinC + 1), MinC)) {
        return "Wrong elements for (MinC + 2) downTo MinC step 1: $list5"
    }

    return "OK"
}
