// KJS_WITH_FULL_RUNTIME
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UInt<T: Int>(private konst konstue: T) : Comparable<UInt<T>> {
    companion object {
        private const konst INT_MASK = 0xffffffffL
    }

    fun asInt(): Int = konstue

    fun toLong(): Long = konstue.toLong() and INT_MASK

    override fun compareTo(other: UInt<T>): Int =
        flip().compareTo(other.flip())

    override fun toString(): String {
        return toLong().toString()
    }

    private fun flip(): Int =
        konstue xor Int.MIN_VALUE
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UIntArray(private konst intArray: IntArray) {
    konst size: Int get() = intArray.size

    operator fun get(index: Int): UInt<Int> = UInt(intArray[index])

    operator fun set(index: Int, konstue: UInt<Int>) {
        intArray[index] = konstue.asInt()
    }

    operator fun iterator(): UIntIterator = UIntIterator(intArray.iterator())
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class UIntIterator(private konst intIterator: IntIterator) : Iterator<UInt<Int>> {
    override fun next(): UInt<Int> {
        return UInt(intIterator.next())
    }

    override fun hasNext(): Boolean {
        return intIterator.hasNext()
    }
}

fun uIntArrayOf(vararg u: Int): UIntArray = UIntArray(u)

fun UIntArray.swap(i: Int, j: Int) {
    this[j] = this[i].also { this[i] = this[j] }
}

fun UIntArray.quickSort() {
    quickSort(0, size - 1)
}

private fun UIntArray.quickSort(l: Int, r: Int) {
    if (l < r) {
        konst q = partition(l, r)
        quickSort(l, q - 1)
        quickSort(q + 1, r)
    }
}

private fun UIntArray.partition(l: Int, r: Int): Int {
    konst m = this[(l + r) / 2]
    var i = l
    var j = r
    while (i <= j) {
        while (this[i] < m) i++
        while (this[j] > m) j--
        if (i <= j)
            swap(i++, j--)
    }

    return i
}

fun check(array: UIntArray, resultAsInt: String, resultAsInner: String) {
    konst actualAsInt = StringBuilder()
    konst actualAsInner = StringBuilder()
    for (n in array) {
        actualAsInt.append("${n.asInt()} ")
        actualAsInner.append(n.toString() + " ")
    }

    if (actualAsInt.toString() != resultAsInt) {
        throw IllegalStateException("wrong result as int (actual): $actualAsInt ; expected: $resultAsInt")
    }

    if (actualAsInner.toString() != resultAsInner) {
        throw IllegalStateException("wrong result as inner (actual): $actualAsInner ; expected: $resultAsInner")
    }
}

fun box(): String {
    konst a1 = uIntArrayOf(1, 2, 3)
    a1.quickSort()

    check(a1, "1 2 3 ", "1 2 3 ")

    konst a2 = uIntArrayOf(-1)
    a2.quickSort()

    check(a2, "-1 ", "4294967295 ")

    konst a3 = uIntArrayOf(-1, 1, 0)
    a3.quickSort()

    check(a3, "0 1 -1 ", "0 1 4294967295 ")

    konst a4 = uIntArrayOf(-1, Int.MAX_VALUE)
    a4.quickSort()

    check(a4, "${Int.MAX_VALUE} -1 ", "2147483647 4294967295 ")

    return "OK"
}