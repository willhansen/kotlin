// IGNORE_ANNOTATIONS

inline class InlineMutableIterator<T>(private konst it: MutableIterator<T>) : MutableIterator<T> {
    override fun hasNext(): Boolean = it.hasNext()
    override fun next(): T = it.next()
    override fun remove() { it.remove() }
}

