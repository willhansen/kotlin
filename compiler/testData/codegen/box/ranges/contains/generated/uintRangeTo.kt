// Auto-generated by GenerateInRangeExpressionTestData. Do not edit!
// WITH_STDLIB



konst range0 = 1u..3u
konst range1 = 3u..1u

konst element0 = 0u
konst element1 = 1u
konst element2 = 2u
konst element3 = 3u
konst element4 = 4u

fun box(): String {
    testR0xE0()
    testR0xE1()
    testR0xE2()
    testR0xE3()
    testR0xE4()
    testR1xE0()
    testR1xE1()
    testR1xE2()
    testR1xE3()
    testR1xE4()
    return "OK"
}

fun testR0xE0() {
    // with possible local optimizations
    if (0u in 1u..3u != range0.contains(0u)) throw AssertionError()
    if (0u !in 1u..3u != !range0.contains(0u)) throw AssertionError()
    if (!(0u in 1u..3u) != !range0.contains(0u)) throw AssertionError()
    if (!(0u !in 1u..3u) != range0.contains(0u)) throw AssertionError()
    // no local optimizations
    if (element0 in 1u..3u != range0.contains(element0)) throw AssertionError()
    if (element0 !in 1u..3u != !range0.contains(element0)) throw AssertionError()
    if (!(element0 in 1u..3u) != !range0.contains(element0)) throw AssertionError()
    if (!(element0 !in 1u..3u) != range0.contains(element0)) throw AssertionError()
}

fun testR0xE1() {
    // with possible local optimizations
    if (1u in 1u..3u != range0.contains(1u)) throw AssertionError()
    if (1u !in 1u..3u != !range0.contains(1u)) throw AssertionError()
    if (!(1u in 1u..3u) != !range0.contains(1u)) throw AssertionError()
    if (!(1u !in 1u..3u) != range0.contains(1u)) throw AssertionError()
    // no local optimizations
    if (element1 in 1u..3u != range0.contains(element1)) throw AssertionError()
    if (element1 !in 1u..3u != !range0.contains(element1)) throw AssertionError()
    if (!(element1 in 1u..3u) != !range0.contains(element1)) throw AssertionError()
    if (!(element1 !in 1u..3u) != range0.contains(element1)) throw AssertionError()
}

fun testR0xE2() {
    // with possible local optimizations
    if (2u in 1u..3u != range0.contains(2u)) throw AssertionError()
    if (2u !in 1u..3u != !range0.contains(2u)) throw AssertionError()
    if (!(2u in 1u..3u) != !range0.contains(2u)) throw AssertionError()
    if (!(2u !in 1u..3u) != range0.contains(2u)) throw AssertionError()
    // no local optimizations
    if (element2 in 1u..3u != range0.contains(element2)) throw AssertionError()
    if (element2 !in 1u..3u != !range0.contains(element2)) throw AssertionError()
    if (!(element2 in 1u..3u) != !range0.contains(element2)) throw AssertionError()
    if (!(element2 !in 1u..3u) != range0.contains(element2)) throw AssertionError()
}

fun testR0xE3() {
    // with possible local optimizations
    if (3u in 1u..3u != range0.contains(3u)) throw AssertionError()
    if (3u !in 1u..3u != !range0.contains(3u)) throw AssertionError()
    if (!(3u in 1u..3u) != !range0.contains(3u)) throw AssertionError()
    if (!(3u !in 1u..3u) != range0.contains(3u)) throw AssertionError()
    // no local optimizations
    if (element3 in 1u..3u != range0.contains(element3)) throw AssertionError()
    if (element3 !in 1u..3u != !range0.contains(element3)) throw AssertionError()
    if (!(element3 in 1u..3u) != !range0.contains(element3)) throw AssertionError()
    if (!(element3 !in 1u..3u) != range0.contains(element3)) throw AssertionError()
}

fun testR0xE4() {
    // with possible local optimizations
    if (4u in 1u..3u != range0.contains(4u)) throw AssertionError()
    if (4u !in 1u..3u != !range0.contains(4u)) throw AssertionError()
    if (!(4u in 1u..3u) != !range0.contains(4u)) throw AssertionError()
    if (!(4u !in 1u..3u) != range0.contains(4u)) throw AssertionError()
    // no local optimizations
    if (element4 in 1u..3u != range0.contains(element4)) throw AssertionError()
    if (element4 !in 1u..3u != !range0.contains(element4)) throw AssertionError()
    if (!(element4 in 1u..3u) != !range0.contains(element4)) throw AssertionError()
    if (!(element4 !in 1u..3u) != range0.contains(element4)) throw AssertionError()
}

fun testR1xE0() {
    // with possible local optimizations
    if (0u in 3u..1u != range1.contains(0u)) throw AssertionError()
    if (0u !in 3u..1u != !range1.contains(0u)) throw AssertionError()
    if (!(0u in 3u..1u) != !range1.contains(0u)) throw AssertionError()
    if (!(0u !in 3u..1u) != range1.contains(0u)) throw AssertionError()
    // no local optimizations
    if (element0 in 3u..1u != range1.contains(element0)) throw AssertionError()
    if (element0 !in 3u..1u != !range1.contains(element0)) throw AssertionError()
    if (!(element0 in 3u..1u) != !range1.contains(element0)) throw AssertionError()
    if (!(element0 !in 3u..1u) != range1.contains(element0)) throw AssertionError()
}

fun testR1xE1() {
    // with possible local optimizations
    if (1u in 3u..1u != range1.contains(1u)) throw AssertionError()
    if (1u !in 3u..1u != !range1.contains(1u)) throw AssertionError()
    if (!(1u in 3u..1u) != !range1.contains(1u)) throw AssertionError()
    if (!(1u !in 3u..1u) != range1.contains(1u)) throw AssertionError()
    // no local optimizations
    if (element1 in 3u..1u != range1.contains(element1)) throw AssertionError()
    if (element1 !in 3u..1u != !range1.contains(element1)) throw AssertionError()
    if (!(element1 in 3u..1u) != !range1.contains(element1)) throw AssertionError()
    if (!(element1 !in 3u..1u) != range1.contains(element1)) throw AssertionError()
}

fun testR1xE2() {
    // with possible local optimizations
    if (2u in 3u..1u != range1.contains(2u)) throw AssertionError()
    if (2u !in 3u..1u != !range1.contains(2u)) throw AssertionError()
    if (!(2u in 3u..1u) != !range1.contains(2u)) throw AssertionError()
    if (!(2u !in 3u..1u) != range1.contains(2u)) throw AssertionError()
    // no local optimizations
    if (element2 in 3u..1u != range1.contains(element2)) throw AssertionError()
    if (element2 !in 3u..1u != !range1.contains(element2)) throw AssertionError()
    if (!(element2 in 3u..1u) != !range1.contains(element2)) throw AssertionError()
    if (!(element2 !in 3u..1u) != range1.contains(element2)) throw AssertionError()
}

fun testR1xE3() {
    // with possible local optimizations
    if (3u in 3u..1u != range1.contains(3u)) throw AssertionError()
    if (3u !in 3u..1u != !range1.contains(3u)) throw AssertionError()
    if (!(3u in 3u..1u) != !range1.contains(3u)) throw AssertionError()
    if (!(3u !in 3u..1u) != range1.contains(3u)) throw AssertionError()
    // no local optimizations
    if (element3 in 3u..1u != range1.contains(element3)) throw AssertionError()
    if (element3 !in 3u..1u != !range1.contains(element3)) throw AssertionError()
    if (!(element3 in 3u..1u) != !range1.contains(element3)) throw AssertionError()
    if (!(element3 !in 3u..1u) != range1.contains(element3)) throw AssertionError()
}

fun testR1xE4() {
    // with possible local optimizations
    if (4u in 3u..1u != range1.contains(4u)) throw AssertionError()
    if (4u !in 3u..1u != !range1.contains(4u)) throw AssertionError()
    if (!(4u in 3u..1u) != !range1.contains(4u)) throw AssertionError()
    if (!(4u !in 3u..1u) != range1.contains(4u)) throw AssertionError()
    // no local optimizations
    if (element4 in 3u..1u != range1.contains(element4)) throw AssertionError()
    if (element4 !in 3u..1u != !range1.contains(element4)) throw AssertionError()
    if (!(element4 in 3u..1u) != !range1.contains(element4)) throw AssertionError()
    if (!(element4 !in 3u..1u) != range1.contains(element4)) throw AssertionError()
}


