// IGNORE_ANNOTATIONS

inline class IT(konst x: Int)

inline class InlineMutableIterator(private konst it: MutableIterator<IT>) : MutableIterator<IT> {
    override fun hasNext(): Boolean = it.hasNext()
    override fun next(): IT = it.next()
    override fun remove() { it.remove() }
}
