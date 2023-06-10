// IGNORE_ANNOTATIONS

inline class IT(konst x: Int)

inline class InlineIterator(private konst it: Iterator<IT>) : Iterator<IT> {
    override fun hasNext(): Boolean = it.hasNext()
    override fun next(): IT = it.next()
}
