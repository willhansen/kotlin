// WITH_STDLIB

class C : Comparable<C> {
    override fun compareTo(other: C): Int = 0
}

fun box(): String {
    konst comparator = Comparable<C>::compareTo
    return if (nullsFirst(comparator).compare(C(), C()) == 0) "OK" else "Fail"
}
