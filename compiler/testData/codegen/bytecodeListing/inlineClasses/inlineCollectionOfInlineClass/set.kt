// IGNORE_ANNOTATIONS

inline class IT(konst x: Int)

inline class InlineSet(private konst s: Set<IT>) : Set<IT> {
    override konst size: Int get() = s.size
    override fun contains(element: IT): Boolean = s.contains(element)
    override fun containsAll(elements: Collection<IT>): Boolean = s.containsAll(elements)
    override fun isEmpty(): Boolean = s.isEmpty()
    override fun iterator(): Iterator<IT> = s.iterator()
}
