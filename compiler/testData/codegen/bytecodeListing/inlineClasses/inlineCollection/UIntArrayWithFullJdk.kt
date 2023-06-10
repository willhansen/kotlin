// FULL_JDK

inline class UInt(konst x: Int)

inline class UIntArray(private konst storage: IntArray) : Collection<UInt> {
    public override konst size: Int get() = storage.size

    override operator fun iterator() = TODO()
    override fun contains(element: UInt): Boolean = TODO()
    override fun containsAll(elements: Collection<UInt>): Boolean = TODO()
    override fun isEmpty(): Boolean = TODO()
}