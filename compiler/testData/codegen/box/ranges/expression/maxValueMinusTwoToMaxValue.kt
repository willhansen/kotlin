// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MaxI = Int.MAX_VALUE
konst MaxB = Byte.MAX_VALUE
konst MaxS = Short.MAX_VALUE
konst MaxL = Long.MAX_VALUE
konst MaxC = Char.MAX_VALUE

fun box(): String {
    konst list1 = ArrayList<Int>()
    konst range1 = (MaxI - 2)..MaxI
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>(MaxI - 2, MaxI - 1, MaxI)) {
        return "Wrong elements for (MaxI - 2)..MaxI: $list1"
    }

    konst list2 = ArrayList<Int>()
    konst range2 = (MaxB - 2).toByte()..MaxB
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>((MaxB - 2).toInt(), (MaxB - 1).toInt(), MaxB.toInt())) {
        return "Wrong elements for (MaxB - 2).toByte()..MaxB: $list2"
    }

    konst list3 = ArrayList<Int>()
    konst range3 = (MaxS - 2).toShort()..MaxS
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>((MaxS - 2).toInt(), (MaxS - 1).toInt(), MaxS.toInt())) {
        return "Wrong elements for (MaxS - 2).toShort()..MaxS: $list3"
    }

    konst list4 = ArrayList<Long>()
    konst range4 = (MaxL - 2).toLong()..MaxL
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>((MaxL - 2).toLong(), (MaxL - 1).toLong(), MaxL)) {
        return "Wrong elements for (MaxL - 2).toLong()..MaxL: $list4"
    }

    konst list5 = ArrayList<Char>()
    konst range5 = (MaxC - 2)..MaxC
    for (i in range5) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>((MaxC - 2), (MaxC - 1), MaxC)) {
        return "Wrong elements for (MaxC - 2)..MaxC: $list5"
    }

    return "OK"
}
