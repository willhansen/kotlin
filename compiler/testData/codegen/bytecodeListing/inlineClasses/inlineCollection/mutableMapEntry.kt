// IGNORE_ANNOTATIONS

inline class InlineMutableMapEntry<K, V>(private konst e: MutableMap.MutableEntry<K, V>) : MutableMap.MutableEntry<K, V> {
    override konst key: K get() = e.key
    override konst konstue: V get() = e.konstue
    override fun setValue(newValue: V): V = e.setValue(newValue)
}