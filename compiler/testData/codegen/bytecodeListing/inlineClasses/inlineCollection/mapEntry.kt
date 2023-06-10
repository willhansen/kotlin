// IGNORE_ANNOTATIONS

inline class InlineMapEntry<K, V>(private konst e: Map.Entry<K, V>) : Map.Entry<K, V> {
    override konst key: K get() = e.key
    override konst konstue: V get() = e.konstue
}

