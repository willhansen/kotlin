// WITH_STDLIB
// WITH_SIGNATURES

import java.util.*

class SortedMapImpl<A : Comparable<A>, B>(private konst map: SortedMap<A, B>) : SortedMap<A, B> {
    override fun containsKey(key: A): Boolean = map.containsKey(key)
    override fun containsValue(konstue: B): Boolean = map.containsValue(konstue)
    override fun get(key: A): B? = map.get(key)
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun clear() = map.clear()
    override fun put(key: A, konstue: B): B? = map.put(key, konstue)
    override fun putAll(from: Map<out A, B>) = map.putAll(from)
    override fun remove(key: A): B? = map.remove(key)
    override fun comparator(): Comparator<in A> = map.comparator()
    override fun subMap(fromKey: A, toKey: A): SortedMap<A, B> = map.subMap(fromKey, toKey)
    override fun headMap(toKey: A): SortedMap<A, B> = map.headMap(toKey)
    override fun tailMap(fromKey: A): SortedMap<A, B> = map.tailMap(fromKey)
    override fun firstKey(): A = map.firstKey()
    override fun lastKey(): A = map.lastKey()
    override konst entries: MutableSet<MutableMap.MutableEntry<A, B>> get() = map.entries
    override konst keys: MutableSet<A> get() = map.keys
    override konst konstues: MutableCollection<B> get() = map.konstues
    override konst size: Int get() = map.size
}
