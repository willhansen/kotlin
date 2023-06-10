// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

class ArraySortedChecker<A, T>(konst array: A, konst comparator: Comparator<in T>) {
    fun <R> checkSorted(sorted: A.() -> R, sortedDescending: A.() -> R, iterator: R.() -> Iterator<T>) {}
}

fun <A, T: Comparable<T>> arrayData(vararg konstues: T, toArray: Array<out T>.() -> A) = ArraySortedChecker<A, T>(konstues.toArray(), naturalOrder())

fun main() {
    with (arrayData("ac", "aD", "aba") { toList().toTypedArray() }) {}
}
