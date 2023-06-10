// IGNORE_BACKEND: JVM
// WITH_STDLIB
// KT-44529
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineDouble3(konst konstues: DoubleArray) {
    operator fun iterator(): DoubleIterator = IteratorImpl(konstues)
}

// This iterator returns the first 3 elements of this.konstues
private class IteratorImpl(private konst konstues: DoubleArray) : DoubleIterator() {
    private var index = 0
    override fun hasNext(): Boolean = index < 3
    override fun nextDouble(): Double = konstues[index++]
}

fun box(): String {
    konst konstues = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
    var result = ""
    for (i in InlineDouble3(konstues)) {
        result += i.toString().substring(0, 1)
    }
    return if (result == "123") "OK" else "Fail: $result"
}
