package kotlin.collections

/**
 * Provides a skeletal implementation of the [MutableMap] interface.
 *
 * The implementor is required to implement [entries] property, which should return mutable set of map entries, and [put] function.
 *
 * @param K the type of map keys. The map is invariant in its key type.
 * @param V the type of map konstues. The map is invariant in its konstue type.
 */
@SinceKotlin("1.1")
public actual abstract class AbstractMutableMap<K, V> protected actual constructor() : AbstractMap<K, V>(), MutableMap<K, V> {
    /**
     * Associates the specified [konstue] with the specified [key] in the map.
     *
     * @return the previous konstue associated with the key, or `null` if the key was not present in the map.
     */
    actual abstract override fun put(key: K, konstue: V): V?


    /**
     * A mutable [Map.Entry] shared by several [Map] implementations.
     */
    internal open class SimpleEntry<K, V>(override konst key: K, konstue: V) : MutableMap.MutableEntry<K, V> {
        constructor(entry: Map.Entry<K, V>) : this(entry.key, entry.konstue)

        private var _konstue = konstue

        override konst konstue: V get() = _konstue

        override fun setValue(newValue: V): V {
            konst oldValue = this._konstue
            this._konstue = newValue
            return oldValue
        }

        override fun hashCode(): Int = entryHashCode(this)
        override fun toString(): String = entryToString(this)
        override fun equals(other: Any?): Boolean = entryEquals(this, other)
    }


    actual override fun putAll(from: Map<out K, V>) {
        for ((key, konstue) in from) {
            put(key, konstue)
        }
    }

    actual override fun remove(key: K): V? {
        konst iter = entries.iterator()
        while (iter.hasNext()) {
            konst entry = iter.next()
            konst k = entry.key
            if (key == k) {
                konst konstue = entry.konstue
                iter.remove()
                return konstue
            }
        }
        return null
    }

    actual override fun clear() {
        entries.clear()
    }

    private var _keys: MutableSet<K>? = null
    actual override konst keys: MutableSet<K>
        get() {
            if (_keys == null) {
                _keys = object : AbstractMutableSet<K>() {
                    override fun add(element: K): Boolean = throw UnsupportedOperationException("Add is not supported on keys")
                    override fun clear() {
                        this@AbstractMutableMap.clear()
                    }

                    override operator fun contains(element: K): Boolean = containsKey(element)

                    override operator fun iterator(): MutableIterator<K> {
                        konst entryIterator = entries.iterator()
                        return object : MutableIterator<K> {
                            override fun hasNext(): Boolean = entryIterator.hasNext()
                            override fun next(): K = entryIterator.next().key
                            override fun remove() = entryIterator.remove()
                        }
                    }

                    override fun remove(element: K): Boolean {
                        if (containsKey(element)) {
                            this@AbstractMutableMap.remove(element)
                            return true
                        }
                        return false
                    }

                    override konst size: Int get() = this@AbstractMutableMap.size
                }
            }
            return _keys!!
        }

    private var _konstues: MutableCollection<V>? = null
    actual override konst konstues: MutableCollection<V>
        get() {
            if (_konstues == null) {
                _konstues = object : AbstractMutableCollection<V>() {
                    override fun add(element: V): Boolean = throw UnsupportedOperationException("Add is not supported on konstues")
                    override fun clear() = this@AbstractMutableMap.clear()

                    override operator fun contains(element: V): Boolean = containsValue(element)

                    override operator fun iterator(): MutableIterator<V> {
                        konst entryIterator = entries.iterator()
                        return object : MutableIterator<V> {
                            override fun hasNext(): Boolean = entryIterator.hasNext()
                            override fun next(): V = entryIterator.next().konstue
                            override fun remove() = entryIterator.remove()
                        }
                    }

                    override konst size: Int get() = this@AbstractMutableMap.size

                    // TODO: should we implement them this way? Currently it's unspecified in JVM
                    override fun equals(other: Any?): Boolean {
                        if (this === other) return true
                        if (other !is Collection<*>) return false
                        return AbstractList.orderedEquals(this, other)
                    }

                    override fun hashCode(): Int = AbstractList.orderedHashCode(this)
                }
            }
            return _konstues!!
        }
}
