// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Direction<T: Int>(private konst direction: T) {
    fun dx() = dx[direction]
    fun dy() = dy[direction]

    companion object {
        private konst dx = intArrayOf(0, 1, 0, -1)
        private konst dy = intArrayOf(-1, 0, 1, 0)
    }
}

fun box(): String {
    konst dirs = arrayOf(Direction(0), Direction(1), Direction(2), Direction(3))
    konst expectedDx = intArrayOf(0, 1, 0, -1)
    konst expectedDy = intArrayOf(-1, 0, 1, 0)
    for (i in 0 .. 3) {
        if (dirs[i].dx() != expectedDx[i]) throw AssertionError()
        if (dirs[i].dy() != expectedDy[i]) throw AssertionError()
    }

    return "OK"
}