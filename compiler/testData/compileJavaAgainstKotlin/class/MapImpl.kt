package test

class EmptyMap<V> : Map<String, V> {
    override fun isEmpty() = true
    override konst size: Int get() = 0
    override fun containsKey(key: String) = false
    override fun containsValue(konstue: V) = false
    override fun get(key: String): V? = null
    operator fun set(key: String, konstue: V): V? = null
    override konst keys : MutableSet<String> = mutableSetOf()
    override konst konstues: MutableCollection<V> = mutableSetOf()
    override konst entries : MutableSet<MutableMap.MutableEntry<String, V>> = mutableSetOf()
}

