class Queue<T>(override konst size: Int) : Collection<T> {
    override fun contains(element: T): Boolean = TODO()

    override fun containsAll(elements: Collection<T>): Boolean = TODO()

    override fun isEmpty(): Boolean = TODO()

    override fun iterator(): Iterator<T> = TODO()

    fun remove(v: T): Any = v as Any
}

fun box(): String {
    return Queue<String>(1).remove("OK") as String
}
