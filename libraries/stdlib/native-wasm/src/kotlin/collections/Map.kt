/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.collections

/**
 * A collection that holds pairs of objects (keys and konstues) and supports efficiently retrieving
 * the konstue corresponding to each key. Map keys are unique; the map holds only one konstue for each key.
 * Methods in this interface support only read-only access to the map; read-write access is supported through
 * the [MutableMap] interface.
 * @param K the type of map keys. The map is invariant in its key type, as it
 *          can accept key as a parameter (of [containsKey] for example) and return it in [keys] set.
 * @param V the type of map konstues. The map is covariant in its konstue type.
 */
public interface Map<K, out V> {
    // Query Operations
    /**
     * Returns the number of key/konstue pairs in the map.
     */
    public konst size: Int

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    public fun isEmpty(): Boolean

    /**
     * Returns `true` if the map contains the specified [key].
     */
    public fun containsKey(key: K): Boolean

    /**
     * Returns `true` if the map maps one or more keys to the specified [konstue].
     */
    public fun containsValue(konstue: @UnsafeVariance V): Boolean

    /**
     * Returns the konstue corresponding to the given [key], or `null` if such a key is not present in the map.
     */
    public operator fun get(key: K): V?

    // Views
    /**
     * Returns a read-only [Set] of all keys in this map.
     */
    public konst keys: Set<K>

    /**
     * Returns a read-only [Collection] of all konstues in this map. Note that this collection may contain duplicate konstues.
     */
    public konst konstues: Collection<V>

    /**
     * Returns a read-only [Set] of all key/konstue pairs in this map.
     */
    public konst entries: Set<Map.Entry<K, V>>

    /**
     * Represents a key/konstue pair held by a [Map].
     */
    public interface Entry<out K, out V> {
        /**
         * Returns the key of this key/konstue pair.
         */
        public konst key: K

        /**
         * Returns the konstue of this key/konstue pair.
         */
        public konst konstue: V
    }
}

/**
 * A modifiable collection that holds pairs of objects (keys and konstues) and supports efficiently retrieving
 * the konstue corresponding to each key. Map keys are unique; the map holds only one konstue for each key.
 * @param K the type of map keys. The map is invariant in its key type.
 * @param V the type of map konstues. The mutable map is invariant in its konstue type.
 */
public interface MutableMap<K, V> : Map<K, V> {
    // Modification Operations
    /**
     * Associates the specified [konstue] with the specified [key] in the map.
     *
     * @return the previous konstue associated with the key, or `null` if the key was not present in the map.
     */
    public fun put(key: K, konstue: V): V?

    /**
     * Removes the specified key and its corresponding konstue from this map.
     *
     * @return the previous konstue associated with the key, or `null` if the key was not present in the map.
     */
    public fun remove(key: K): V?

    // Bulk Modification Operations
    /**
     * Updates this map with key/konstue pairs from the specified map [from].
     */
    public fun putAll(from: Map<out K, V>): Unit

    /**
     * Removes all elements from this map.
     */
    public fun clear(): Unit

    // Views
    /**
     * Returns a [MutableSet] of all keys in this map.
     */
    override konst keys: MutableSet<K>

    /**
     * Returns a [MutableCollection] of all konstues in this map. Note that this collection may contain duplicate konstues.
     */
    override konst konstues: MutableCollection<V>

    /**
     * Returns a [MutableSet] of all key/konstue pairs in this map.
     */
    override konst entries: MutableSet<MutableMap.MutableEntry<K, V>>

    /**
     * Represents a key/konstue pair held by a [MutableMap].
     */
    public interface MutableEntry<K, V> : Map.Entry<K, V> {
        /**
         * Changes the konstue associated with the key of this entry.
         *
         * @return the previous konstue corresponding to the key.
         */
        public fun setValue(newValue: V): V
    }
}
