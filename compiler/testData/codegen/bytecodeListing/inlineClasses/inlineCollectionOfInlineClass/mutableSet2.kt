// IGNORE_ANNOTATIONS

inline class IT(konst x: Int)

inline class IT2(konst x: IT)

inline class InlineMutableSet2(private konst ms: MutableSet<IT2>) : MutableSet<IT2> {
    override konst size: Int get() = ms.size
    override fun contains(element: IT2): Boolean = ms.contains(element)
    override fun containsAll(elements: Collection<IT2>): Boolean = ms.containsAll(elements)
    override fun isEmpty(): Boolean = ms.isEmpty()
    override fun add(element: IT2): Boolean = ms.add(element)
    override fun addAll(elements: Collection<IT2>): Boolean = ms.addAll(elements)
    override fun clear() { ms.clear() }
    override fun iterator(): MutableIterator<IT2> = ms.iterator()
    override fun remove(element: IT2): Boolean = ms.remove(element)
    override fun removeAll(elements: Collection<IT2>): Boolean = ms.removeAll(elements)
    override fun retainAll(elements: Collection<IT2>): Boolean = ms.retainAll(elements)
}
