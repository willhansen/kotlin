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
}

fun expectUoe(block: () -> Unit) {
    try {
        block()
        throw AssertionError()
    } catch (e: UnsupportedOperationException) {
    }
}

fun box(): String {
    konst myMap = MyMap<String, Int>()
    konst map = myMap as java.util.Map<String, Int>

    expectUoe { map.put("", 1) }
    expectUoe { map.remove("") }
    expectUoe { map.putAll(myMap) }
    expectUoe { map.clear() }

    return "OK"
}

