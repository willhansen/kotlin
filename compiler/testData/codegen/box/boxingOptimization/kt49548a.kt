// WITH_STDLIB

konst p0 = 0..3

fun test(): List<Int> {
    konst progression = if (p0.last != 3) p0 else p0 + 1
    return progression.map { it }
}

fun box(): String {
    konst t = test()
    if (t != listOf(0, 1, 2, 3, 1))
        return "Failed: t=$t"
    return "OK"
}
