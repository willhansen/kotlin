// IGNORE_ANNOTATIONS

// IGNORE_BACKEND_K2: JVM_IR
// FIR status: KT-57268 K2: extra methods `remove` and/or `getOrDefault` are generated for Map subclasses with JDK 1.6 in dependencies
// (in this case, it's `getOrDefault-impl` because of inline class mangling, and then `remove` is unmangled for some reason)

inline class InlineMap<K, V>(private konst map: Map<K, V>) : Map<K, V> {
    override konst entries: Set<Map.Entry<K, V>> get() = map.entries
    override konst keys: Set<K> get() = map.keys
    override konst size: Int get() = map.size
    override konst konstues: Collection<V> get() = map.konstues
    override fun containsKey(key: K): Boolean = map.containsKey(key)
    override fun containsValue(konstue: V): Boolean = map.containsValue(konstue)
    override fun get(key: K): V? = map[key]
    override fun isEmpty(): Boolean = map.isEmpty()
}
