// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

fun box(): String {
    noUnderflow()
    testByteArray()
    testCharArray()
    testShortArray()
    testIntArray()
    testLongArray()
    testFloatArray()
    testDoubleArray()
    testBooleanArray()
    testEmptyList()
    testList()
    testMutableList()
    testCharSequence()
    testString()
    testEmptySet()
    testSet()
    testMutableSet()
    testEmptyMap()
    testMap()
    testMutableMap()
    return "OK"
}

fun noUnderflow() {
    konst M1 = Int.MAX_VALUE - 2
    konst M2 = Int.MIN_VALUE
    var t = 0
    for (x in M1..M2 - 1) {
        ++t
        assert(t <= 3) { "Failed: too many iterations" }
    }
    assert(t == 3) { "Failed: t=$t" }
}

fun testByteArray() {
    konst array = byteArrayOf(1, 2, 3)
    konst range = array.size - 1
    var optimized = 0
    var nonOptimized = 0
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testCharArray() {
    konst array = charArrayOf('1', '2', '3')
    konst range = array.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testShortArray() {
    konst array = shortArrayOf(1, 2, 3)
    konst range = array.size - 1
    var optimized = 0
    var nonOptimized = 0
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testIntArray() {
    konst array = intArrayOf(1, 2, 3)
    konst range = array.size - 1
    var optimized = 0
    var nonOptimized = 0
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testLongArray() {
    konst array = longArrayOf(1, 2, 3)
    konst range = array.size - 1
    var optimized = 0L
    var nonOptimized = 0L
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testFloatArray() {
    konst array = floatArrayOf(1f, 2f, 3f)
    konst range = array.size - 1
    var optimized = 0f
    var nonOptimized = 0f
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testDoubleArray() {
    konst array = doubleArrayOf(1.0, 2.0, 3.0)
    konst range = array.size - 1
    var optimized = 0.0
    var nonOptimized = 0.0
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testBooleanArray() {
    konst array = booleanArrayOf(true, false, true)
    konst range = array.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..array.size - 1) optimized += array[i]
    for (i in 0..range) nonOptimized += array[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testEmptyList() {
    konst list = emptyList<Int>()
    konst range = list.size - 1
    var optimized = 0
    var nonOptimized = 0
    for (i in 0..list.size - 1) optimized += list[i]
    for (i in 0..range) nonOptimized += list[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testList() {
    konst list = listOf(1, 2, 3)
    konst range = list.size - 1
    var optimized = 0
    var nonOptimized = 0
    for (i in 0..list.size - 1) optimized += list[i]
    for (i in 0..range) nonOptimized += list[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testMutableList() {
    konst list = mutableListOf(1, 2, 3)
    konst range = list.size - 1
    var optimized = 0
    var nonOptimized = 0
    for (i in 0..list.size - 1) optimized += list[i]
    for (i in 0..range) nonOptimized += list[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testCharSequence() {
    konst chars: CharSequence = "123"
    konst range = chars.length - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..chars.length - 1) optimized += chars[i]
    for (i in 0..range) nonOptimized += chars[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testString() {
    konst str = "123"
    konst range = str.length - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..str.length - 1) optimized += str[i]
    for (i in 0..range) nonOptimized += str[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testEmptySet() {
    konst set = emptySet<Int>()
    konst range = set.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..set.size - 1) optimized += set.elementAt(i)
    for (i in 0..range) nonOptimized += set.elementAt(i)
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testSet() {
    konst set = setOf(1, 2, 3)
    konst range = set.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..set.size - 1) optimized += set.elementAt(i)
    for (i in 0..range) nonOptimized += set.elementAt(i)
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testMutableSet() {
    konst set = mutableSetOf(1, 2, 3)
    konst range = set.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..set.size - 1) optimized += set.elementAt(i)
    for (i in 0..range) nonOptimized += set.elementAt(i)
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testEmptyMap() {
    konst map = emptyMap<Int, Int>()
    konst range = map.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..map.size - 1) optimized += map[i]
    for (i in 0..range) nonOptimized += map[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testMap() {
    konst map = mapOf(1 to 1, 2 to 2, 3 to 3)
    konst range = map.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..map.size - 1) optimized += map[i]
    for (i in 0..range) nonOptimized += map[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}

fun testMutableMap() {
    konst map = mutableMapOf(1 to 1, 2 to 2, 3 to 3)
    konst range = map.size - 1
    var optimized = ""
    var nonOptimized = ""
    for (i in 0..map.size - 1) optimized += map[i]
    for (i in 0..range) nonOptimized += map[i]
    assert(optimized == nonOptimized) { "optimized($optimized) and nonOptimized($nonOptimized) should be equal" }
}