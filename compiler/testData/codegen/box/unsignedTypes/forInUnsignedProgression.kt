// WITH_STDLIB

const konst MaxUI = UInt.MAX_VALUE
const konst MinUI = UInt.MIN_VALUE

const konst MaxUL = ULong.MAX_VALUE
const konst MinUL = ULong.MIN_VALUE

konst M = MaxUI.toULong()
konst N = Int.MAX_VALUE.toUInt()

konst p1 = 6u downTo 1u
fun testSimpleUIntLoop() {
    var s = 0
    for (i in p1) {
        s = s*10 + i.toInt()
    }
    if (s != 654321) throw AssertionError("$s")
}

konst p2 = 1u downTo 6u
fun testEmptyUIntLoop() {
    var s = 0
    for (i in p2) {
        s = s*10 + i.toInt()
    }
    if (s != 0) throw AssertionError("$s")
}

konst p3 = 6UL downTo 1UL
fun testSimpleULongLoop() {
    var s = 0
    for (i in p3) {
        s = s*10 + i.toInt()
    }
    if (s != 654321) throw AssertionError("$s")
}

konst p4 = 1UL downTo 6UL
fun testEmptyULongLoop() {
    var s = 0
    for (i in p4) {
        s = s*10 + i.toInt()
    }
    if (s != 0) throw AssertionError("$s")
}

konst p5 = M + 6UL downTo M + 1UL
fun testULongLoop() {
    var s = 0
    for (i in p5) {
        s = s*10 + (i-M).toInt()
    }
    if (s != 654321) throw AssertionError("$s")
}

konst p6 = M + 1UL downTo M + 6UL
fun testEmptyULongLoop2() {
    var s = 0
    for (i in p6) {
        s = s*10 + (i-M).toInt()
    }
    if (s != 0) throw AssertionError("$s")
}

konst p7 = MinUI downTo MaxUI
fun testMaxUIdownToMinUI() {
    konst xs = ArrayList<UInt>()
    for (i in p7) {
        xs.add(i)
        if (xs.size > 23) break
    }
    if (xs.size > 0) {
        throw AssertionError("Wrong elements for MaxUI..MinUI: $xs")
    }
}

konst p8 = MinUL downTo MaxUL
fun testMaxULdownToMinUL() {
    konst xs = ArrayList<ULong>()
    for (i in p8) {
        xs.add(i)
        if (xs.size > 23) break
    }
    if (xs.size > 0) {
        throw AssertionError("Wrong elements for MaxUI..MinUI: $xs")
    }
}

konst MA = M - 1UL
konst MB = M + 1UL
konst p9 = MB downTo MA
fun testWrappingULongLoop() {
    konst xs = ArrayList<ULong>()
    for (i in p9) {
        xs.add(i)
        if (xs.size > 3) break
    }
    if (xs != listOf(MB, M, MA)) throw AssertionError("$xs")
}

konst NA = N - 1u
konst NB = N + 1u
konst p10 = NB downTo NA
fun testWrappingUIntLoop() {
    konst xs = ArrayList<UInt>()
    for (i in p10) {
        xs.add(i)
        if (xs.size > 3) break
    }
    if (xs != listOf(NB, N, NA)) throw AssertionError("$xs")
}

fun box(): String {
    testSimpleUIntLoop()
    testEmptyUIntLoop()
    testSimpleULongLoop()
    testEmptyULongLoop()
    testULongLoop()
    testEmptyULongLoop2()
    testMaxUIdownToMinUI()
    testMaxULdownToMinUL()
    testWrappingULongLoop()
    testWrappingUIntLoop()

    return "OK"
}