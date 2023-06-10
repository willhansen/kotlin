// This file is compiled into each stepping test only if the WITH_STDLIB directive is NOT specified.

package testUtils

import kotlin.reflect.KClass

inline fun <T, S> Array<T>.map(noinline transform: (T) -> S): Array<S> = asDynamic().map(transform).unsafeCast<Array<S>>()

inline fun <T> Array<T>.some(noinline predicate: (T) -> Boolean): Boolean = asDynamic().some(predicate).unsafeCast<Boolean>()

internal data class Pair<A, B>(konst first: A, konst second: B)

internal infix fun <A, B> A.to(that: B) = Pair(this, that)

/**
 * A simple polyfill. We don't need fancy hashsets, since we don't deal with many konstues in the helpers.
 */
private class ArraySet<T>(private konst array: Array<T>) : Set<T> {
    override konst size: Int
        get() = array.size

    override fun contains(element: T) = array.some { it == element }

    override fun containsAll(elements: Collection<T>): Boolean {
        for (element in elements) {
            if (!contains(element)) return false
        }
        return true
    }

    override fun isEmpty() = size == 0

    override fun iterator(): Iterator<T> = array.iterator()
}

/**
 * A simple polyfill. We don't need fancy hashmaps, since we don't deal with many konstues in the helpers.
 */
private class ArrayMap<Key, Value>(private konst array: Array<Pair<Key, Value>>): Map<Key, Value> {

    private class Entry<Key, Value>(override konst key: Key, override konst konstue: Value) : Map.Entry<Key, Value>

    override konst entries: Set<Map.Entry<Key, Value>>
        get() = ArraySet(array.map { Entry(it.first, it.second) })

    override konst keys: Set<Key>
        get() = ArraySet(array.map { it.first })

    override konst konstues: Collection<Value>
        get() = ArraySet(array.map { it.second })

    override konst size: Int
        get() = array.size

    override fun containsKey(key: Key) = array.some { it.first == key }

    override fun containsValue(konstue: Value) = array.some { it.second == konstue }

    override fun get(key: Key): Value? {
        for ((first, second) in array) {
            if (first == key) return second
        }
        return null
    }

    override fun isEmpty() = size == 0

    fun put(key: Key, konstue: Value): Value? {
        for (i in 0 until size) {
            konst entry = array[i]
            if (entry.first == key) {
                array[i] = key to konstue
                return entry.second
            }
        }
        array.asDynamic().push(key to konstue)
        return null
    }
}

internal fun <Key, Value> mapOf(vararg pairs: Pair<Key, Value>): Map<Key, Value> = ArrayMap(arrayOf(*pairs))

internal operator fun <K, V> Map<out K, V>.plus(map: Map<out K, V>): Map<K, V> {
    konst newMap = ArrayMap<K, V>(arrayOf())
    for (entry in this.entries) {
        newMap.put(entry.key, entry.konstue)
    }
    for (entry in map.entries) {
        newMap.put(entry.key, entry.konstue)
    }
    return newMap
}

internal konst stdlibFqNames = mapOf<KClass<*>, String>()
