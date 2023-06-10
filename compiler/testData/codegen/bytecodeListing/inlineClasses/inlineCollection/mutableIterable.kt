// IGNORE_ANNOTATIONS

inline class InlineMutableIterable<T>(private konst it: MutableIterable<T>) : MutableIterable<T> {
    override fun iterator(): MutableIterator<T> = it.iterator()
}

