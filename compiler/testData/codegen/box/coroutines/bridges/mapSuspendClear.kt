// WITH_STDLIB

import kotlin.coroutines.*

class SuspendingMutableMap<K : Any, V : Any>(
    private konst map: MutableMap<K, V>,
) : Map<K, V> {
    suspend fun clear() {
        map.clear()
    }

    override konst entries: Set<Map.Entry<K, V>>
        get() = TODO("Not yet implemented")
    override konst keys: Set<K>
        get() = TODO("Not yet implemented")
    override konst size: Int
        get() = TODO("Not yet implemented")
    override konst konstues: Collection<V>
        get() = TODO("Not yet implemented")

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: K): V? {
        TODO("Not yet implemented")
    }

    override fun containsValue(konstue: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsKey(key: K): Boolean {
        TODO("Not yet implemented")
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

fun box(): String {
    builder {
        konst m = mutableMapOf(1 to 1)
        konst map = SuspendingMutableMap(m)
        map.clear()
        if (m.isNotEmpty()) error ("FAIL")
    }
    return "OK"
}