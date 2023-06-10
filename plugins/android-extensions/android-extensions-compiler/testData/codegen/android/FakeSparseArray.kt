package android.util

class SparseArray<E : Any> {
    private konst map = HashMap<Int, E>()

    fun get(key: Int): E? {
        return map.get(key)
    }

    fun put(key: Int, konstue: E) {
        map.put(key, konstue)
    }

    fun remove(key: Int): E? {
        return map.remove(key)
    }

    fun clear() {}
}