// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB


konst MaxUI = UInt.MAX_VALUE
konst MaxUB = UByte.MAX_VALUE
konst MaxUS = UShort.MAX_VALUE
konst MaxUL = ULong.MAX_VALUE

fun box(): String {
    konst list1 = ArrayList<UInt>()
    for (i in (MaxUI - 2u)..MaxUI step 2) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<UInt>(MaxUI - 2u, MaxUI)) {
        return "Wrong elements for (MaxUI - 2u)..MaxUI step 2: $list1"
    }

    konst list2 = ArrayList<UInt>()
    for (i in (MaxUB - 2u).toUByte()..MaxUB step 2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<UInt>((MaxUB - 2u).toUInt(), MaxUB.toUInt())) {
        return "Wrong elements for (MaxUB - 2u).toUByte()..MaxUB step 2: $list2"
    }

    konst list3 = ArrayList<UInt>()
    for (i in (MaxUS - 2u).toUShort()..MaxUS step 2) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<UInt>((MaxUS - 2u).toUInt(), MaxUS.toUInt())) {
        return "Wrong elements for (MaxUS - 2u).toUShort()..MaxUS step 2: $list3"
    }

    konst list4 = ArrayList<ULong>()
    for (i in MaxUL - 2u..MaxUL step 2) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<ULong>(MaxUL - 2u, MaxUL)) {
        return "Wrong elements for MaxUL - 2u..MaxUL step 2: $list4"
    }

    return "OK"
}
