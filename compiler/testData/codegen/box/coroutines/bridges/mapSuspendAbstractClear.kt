// WITH_STDLIB

import kotlin.coroutines.*

abstract class SuspendingMutableMap<K : Any, V : Any>(
    protected konst map: MutableMap<K, V>,
) : Map<K, V> {
    abstract suspend fun clear()

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
        konst map = object: SuspendingMutableMap<Int, Int>(m) {
            override suspend fun clear() {
                map.clear()
            }
        }
        map.clear()
        if (m.isNotEmpty()) error ("FAIL")
    }
    return "OK"
}