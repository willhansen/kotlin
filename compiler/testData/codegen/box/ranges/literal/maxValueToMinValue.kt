// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MaxI = Int.MAX_VALUE
konst MinI = Int.MIN_VALUE
konst MaxB = Byte.MAX_VALUE
konst MinB = Byte.MIN_VALUE
konst MaxS = Short.MAX_VALUE
konst MinS = Short.MIN_VALUE
konst MaxL = Long.MAX_VALUE
konst MinL = Long.MIN_VALUE
konst MaxC = Char.MAX_VALUE
konst MinC = Char.MIN_VALUE

fun box(): String {
    konst list1 = ArrayList<Int>()
    for (i in MaxI..MinI) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>()) {
        return "Wrong elements for MaxI..MinI: $list1"
    }

    konst list2 = ArrayList<Int>()
    for (i in MaxB..MinB) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>()) {
        return "Wrong elements for MaxB..MinB: $list2"
    }

    konst list3 = ArrayList<Int>()
    for (i in MaxS..MinS) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>()) {
        return "Wrong elements for MaxS..MinS: $list3"
    }

    konst list4 = ArrayList<Long>()
    for (i in MaxL..MinL) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>()) {
        return "Wrong elements for MaxL..MinL: $list4"
    }

    konst list5 = ArrayList<Char>()
    for (i in MaxC..MinC) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>()) {
        return "Wrong elements for MaxC..MinC: $list5"
    }

    return "OK"
}
