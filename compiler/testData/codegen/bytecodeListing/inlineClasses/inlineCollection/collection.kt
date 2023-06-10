// IGNORE_ANNOTATIONS

inline class InlineCollection<T>(private konst c: Collection<T>) : Collection<T> {
    override konst size: Int get() = c.size
    override fun contains(element: T): Boolean = c.contains(element)
    override fun containsAll(elements: Collection<T>): Boolean = c.containsAll(elements)
    override fun isEmpty(): Boolean = c.isEmpty()
    override fun iterator(): Iterator<T> = c.iterator()
}

