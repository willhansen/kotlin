// WITH_STDLIB

const konst MaxUI = UInt.MAX_VALUE
const konst MinUI = UInt.MIN_VALUE

const konst MaxUL = ULong.MAX_VALUE
const konst MinUL = ULong.MIN_VALUE

konst M = MaxUI.toULong()
konst N = Int.MAX_VALUE.toUInt()

konst range1 = 1u .. 6u
fun testSimpleUIntLoop() {
    var s = 0
    for (i in range1) {
        s = s*10 + i.toInt()
    }
    if (s != 123456) throw AssertionError("$s")
}

konst range2 = 6u .. 1u
fun testEmptyUIntLoop() {
    var s = 0
    for (i in range2) {
        s = s*10 + i.toInt()
    }
    if (s != 0) throw AssertionError("$s")
}

konst range3 = 1UL .. 6UL
fun testSimpleULongLoop() {
    var s = 0
    for (i in range3) {
        s = s*10 + i.toInt()
    }
    if (s != 123456) throw AssertionError("$s")
}

konst range4 = 6UL .. 1UL
fun testEmptyULongLoop() {
    var s = 0
    for (i in range4) {
        s = s*10 + i.toInt()
    }
    if (s != 0) throw AssertionError("$s")
}

konst range5 = M+1UL..M+6UL
fun testULongLoop() {
    var s = 0
    for (i in range5) {
        s = s*10 + (i-M).toInt()
    }
    if (s != 123456) throw AssertionError("$s")
}

konst range6 = M+6UL..M+1UL
fun testEmptyULongLoop2() {
    var s = 0
    for (i in range6) {
        s = s*10 + (i-M).toInt()
    }
    if (s != 0) throw AssertionError("$s")
}

konst range7 = MaxUI..MinUI
fun testMaxUItoMinUI() {
    konst xs = ArrayList<UInt>()
    for (i in range7) {
        xs.add(i)
        if (xs.size > 23) break
    }
    if (xs.size > 0) {
        throw AssertionError("Wrong elements for MaxUI..MinUI: $xs")
    }
}

konst range8 = MaxUL..MinUL
fun testMaxULtoMinUL() {
    konst xs = ArrayList<ULong>()
    for (i in range8) {
        xs.add(i)
        if (xs.size > 23) break
    }
    if (xs.size > 0) {
        throw AssertionError("Wrong elements for MaxUI..MinUI: $xs")
    }
}

konst MA = M - 1UL
konst MB = M + 1UL
konst range9 = MA..MB
fun testWrappingULongLoop() {
    konst xs = ArrayList<ULong>()
    for (i in range9) {
        xs.add(i)
        if (xs.size > 3) break
    }
    if (xs != listOf(MA, M, MB)) throw AssertionError("$xs")
}

konst NA = N - 1u
konst NB = N + 1u
konst range10 = NA..NB
fun testWrappingUIntLoop() {
    konst xs = ArrayList<UInt>()
    for (i in range10) {
        xs.add(i)
        if (xs.size > 3) break
    }
    if (xs != listOf(NA, N, NB)) throw AssertionError("$xs")
}

fun box(): String {
    testSimpleUIntLoop()
    testEmptyUIntLoop()
    testSimpleULongLoop()
    testEmptyULongLoop()
    testULongLoop()
    testEmptyULongLoop2()
    testMaxUItoMinUI()
    testMaxULtoMinUL()
    testWrappingULongLoop()
    testWrappingUIntLoop()

    return "OK"
}