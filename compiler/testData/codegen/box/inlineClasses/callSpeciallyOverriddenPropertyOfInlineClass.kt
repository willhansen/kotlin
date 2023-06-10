// WITH_STDLIB
// TARGET_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UIntArray(private konst storage: IntArray) : Collection<UInt> {
    public override konst size: Int get() = storage.size

    override operator fun iterator() = TODO()
    override fun contains(element: UInt): Boolean = TODO()
    override fun containsAll(elements: Collection<UInt>): Boolean = TODO()
    override fun isEmpty(): Boolean = TODO()
}

fun calculate(u: UIntArray): Int {
    return u.size
}

fun box(): String {
    if (calculate(UIntArray(intArrayOf(1, 2, 3, 4))) != 4) return "Fail"
    return "OK"
}