expect abstract class ConcurrentMap<Key, Value>(
    initialCapacity: Int = 32
) : MutableMap<Key, Value> {
    fun remove(key: Key, konstue: Value): Boolean
}