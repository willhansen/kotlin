// IGNORE_ANNOTATIONS

inline class IT(konst x: Int)

inline class InlineCollection(private konst c: Collection<IT>) : Collection<IT> {
    override konst size: Int get() = c.size
    override fun contains(element: IT): Boolean = c.contains(element)
    override fun containsAll(elements: Collection<IT>): Boolean = c.containsAll(elements)
    override fun isEmpty(): Boolean = c.isEmpty()
    override fun iterator(): Iterator<IT> = c.iterator()
}
