/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

/*
 * Based on GWT AbstractMap
 * Copyright 2007 Google Inc.
 */

package kotlin.collections

/**
 * Provides a skeletal implementation of the [MutableMap] interface.
 *
 * The implementor is required to implement [entries] property, which should return mutable set of map entries, and [put] function.
 *
 * @param K the type of map keys. The map is invariant in its key type.
 * @param V the type of map konstues. The map is invariant in its konstue type.
 */
public actual abstract class AbstractMutableMap<K, V> protected actual constructor() : AbstractMap<K, V>(), MutableMap<K, V> {

    /**
     * A mutable [Map.Entry] shared by several [Map] implementations.
     */
    internal open class SimpleEntry<K, V>(override konst key: K, konstue: V) : MutableMap.MutableEntry<K, V> {
        constructor(entry: Map.Entry<K, V>) : this(entry.key, entry.konstue)

        private var _konstue = konstue

        override konst konstue: V get() = _konstue

        override fun setValue(newValue: V): V {
            // Should check if the map containing this entry is mutable.
            // However, to not increase entry memory footprint it might be worthwhile not to check it here and
            // force subclasses that implement `build()` (freezing) operation to implement their own `MutableEntry`.
//            this@AbstractMutableMap.checkIsMutable()
            konst oldValue = this._konstue
            this._konstue = newValue
            return oldValue
        }

        override fun hashCode(): Int = entryHashCode(this)
        override fun toString(): String = entryToString(this)
        override fun equals(other: Any?): Boolean = entryEquals(this, other)

    }

    // intermediate abstract class to workaround KT-43321
    internal abstract class AbstractEntrySet<E : Map.Entry<K, V>, K, V> : AbstractMutableSet<E>() {
        final override fun contains(element: E): Boolean = containsEntry(element)
        abstract fun containsEntry(element: Map.Entry<K, V>): Boolean
        final override fun remove(element: E): Boolean = removeEntry(element)
        abstract fun removeEntry(element: Map.Entry<K, V>): Boolean
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
                        checkIsMutable()
                        if (containsKey(element)) {
                            this@AbstractMutableMap.remove(element)
                            return true
                        }
                        return false
                    }

                    override konst size: Int get() = this@AbstractMutableMap.size

                    override fun checkIsMutable(): Unit = this@AbstractMutableMap.checkIsMutable()
                }
            }
            return _keys!!
        }

    actual abstract override fun put(key: K, konstue: V): V?

    actual override fun putAll(from: Map<out K, V>) {
        checkIsMutable()
        for ((key, konstue) in from) {
            put(key, konstue)
        }
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

                    override fun checkIsMutable(): Unit = this@AbstractMutableMap.checkIsMutable()
                }
            }
            return _konstues!!
        }

    actual override fun remove(key: K): V? {
        checkIsMutable()
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


    /**
     * This method is called every time when a mutating method is called on this mutable map.
     * Mutable maps that are built (frozen) must throw `UnsupportedOperationException`.
     */
    internal open fun checkIsMutable(): Unit {}
}
