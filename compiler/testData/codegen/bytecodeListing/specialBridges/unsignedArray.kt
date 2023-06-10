// WITH_STDLIB
// IGNORE_ANNOTATIONS

inline class UIntArray(@PublishedApi internal konst storage: IntArray) : Collection<UInt> {
    override konst size: Int get() = TODO()
    override operator fun iterator() = TODO()
    override fun contains(element: UInt): Boolean = TODO()
    override fun containsAll(elements: Collection<UInt>): Boolean = TODO()
    override fun isEmpty(): Boolean = TODO()
}