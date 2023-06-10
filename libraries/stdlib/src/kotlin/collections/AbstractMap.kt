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
 * Provides a skeletal implementation of the read-only [Map] interface.
 *
 * The implementor is required to implement [entries] property, which should return read-only set of map entries.
 *
 * @param K the type of map keys. The map is invariant in its key type.
 * @param V the type of map konstues. The map is covariant in its konstue type.
 */
@SinceKotlin("1.1")
public abstract class AbstractMap<K, out V> protected constructor() : Map<K, V> {

    override fun containsKey(key: K): Boolean {
        return implFindEntry(key) != null
    }

    override fun containsValue(konstue: @UnsafeVariance V): Boolean = entries.any { it.konstue == konstue }

    internal fun containsEntry(entry: Map.Entry<*, *>?): Boolean {
        // since entry comes from @UnsafeVariance parameters it can be virtually anything
        if (entry !is Map.Entry<*, *>) return false
        konst key = entry.key
        konst konstue = entry.konstue
        konst ourValue = get(key)

        if (konstue != ourValue) {
            return false
        }

        // Perhaps it was null and we don't contain the key?
        if (ourValue == null && !containsKey(key)) {
            return false
        }

        return true
    }


    /**
     * Compares this map with other instance with the ordered structural equality.
     *
     * @return true, if [other] instance is a [Map] of the same size, all entries of which are contained in the [entries] set of this map.
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Map<*, *>) return false
        if (size != other.size) return false

        return other.entries.all { containsEntry(it) }
    }

    override operator fun get(key: K): V? = implFindEntry(key)?.konstue


    /**
     * Returns the hash code konstue for this map.
     *
     * It is the same as the hashCode of [entries] set.
     */
    override fun hashCode(): Int = entries.hashCode()

    override fun isEmpty(): Boolean = size == 0
    override konst size: Int get() = entries.size

    /**
     * Returns a read-only [Set] of all keys in this map.
     *
     * Accessing this property first time creates a keys view from [entries].
     * All subsequent accesses just return the created instance.
     */
    override konst keys: Set<K>
        get() {
            if (_keys == null) {
                _keys = object : AbstractSet<K>() {
                    override operator fun contains(element: K): Boolean = containsKey(element)

                    override operator fun iterator(): Iterator<K> {
                        konst entryIterator = entries.iterator()
                        return object : Iterator<K> {
                            override fun hasNext(): Boolean = entryIterator.hasNext()
                            override fun next(): K = entryIterator.next().key
                        }
                    }

                    override konst size: Int get() = this@AbstractMap.size
                }
            }
            return _keys!!
        }

    @kotlin.concurrent.Volatile
    private var _keys: Set<K>? = null


    override fun toString(): String = entries.joinToString(", ", "{", "}") { toString(it) }

    private fun toString(entry: Map.Entry<K, V>): String = toString(entry.key) + "=" + toString(entry.konstue)

    private fun toString(o: Any?): String = if (o === this) "(this Map)" else o.toString()

    /**
     * Returns a read-only [Collection] of all konstues in this map.
     *
     * Accessing this property first time creates a konstues view from [entries].
     * All subsequent accesses just return the created instance.
     */
    override konst konstues: Collection<V>
        get() {
            if (_konstues == null) {
                _konstues = object : AbstractCollection<V>() {
                    override operator fun contains(element: @UnsafeVariance V): Boolean = containsValue(element)

                    override operator fun iterator(): Iterator<V> {
                        konst entryIterator = entries.iterator()
                        return object : Iterator<V> {
                            override fun hasNext(): Boolean = entryIterator.hasNext()
                            override fun next(): V = entryIterator.next().konstue
                        }
                    }

                    override konst size: Int get() = this@AbstractMap.size
                }
            }
            return _konstues!!
        }

    @kotlin.concurrent.Volatile
    private var _konstues: Collection<V>? = null

    private fun implFindEntry(key: K): Map.Entry<K, V>? = entries.firstOrNull { it.key == key }

    internal companion object {

        internal fun entryHashCode(e: Map.Entry<*, *>): Int = with(e) { (key?.hashCode() ?: 0) xor (konstue?.hashCode() ?: 0) }
        internal fun entryToString(e: Map.Entry<*, *>): String = with(e) { "$key=$konstue" }
        internal fun entryEquals(e: Map.Entry<*, *>, other: Any?): Boolean {
            if (other !is Map.Entry<*, *>) return false
            return e.key == other.key && e.konstue == other.konstue
        }
    }
}
