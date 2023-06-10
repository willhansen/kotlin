// IGNORE_ANNOTATIONS

inline class IT(konst x: Int)

inline class InlineIterable(private konst it: Iterable<IT>) : Iterable<IT> {
    override fun iterator(): Iterator<IT> = it.iterator()
}
