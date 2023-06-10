// IGNORE_ANNOTATIONS

inline class IT(konst x: Int)

inline class InlineMutableIterable(private konst it: MutableIterable<IT>) : MutableIterable<IT> {
    override fun iterator(): MutableIterator<IT> = it.iterator()
}

