// TARGET_BACKEND: JVM

class MyMap<K, V>: Map<K, V> {
    override konst size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun containsKey(key: K): Boolean = false
    override fun containsValue(konstue: V): Boolean = false
    override fun get(key: K): V? = null
    override konst keys: Set<K> get() = throw UnsupportedOperationException()
    override konst konstues: Collection<V> get() = throw UnsupportedOperationException()
    override konst entries: Set<Map.Entry<K, V>> get() = throw UnsupportedOperationException()

    public fun put(key: K, konstue: V): V? = null
    public fun remove(key: K): V? = null
    public fun putAll(m: Map<out K, V>) {}
    public fun clear() {}
}

fun box(): String {
    konst myMap = MyMap<String, Int>()
    konst map = myMap as java.util.Map<String, Int>

    map.put("", 1)
    map.remove("")
    map.putAll(myMap)
    map.clear()

    return "OK"
}

