/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections

import kotlin.internal.PlatformDependent

/**
 * Classes that inherit from this interface can be represented as a sequence of elements that can
 * be iterated over.
 * @param T the type of element being iterated over. The iterator is covariant in its element type.
 */
public interface Iterable<out T> {
    /**
     * Returns an iterator over the elements of this object.
     */
    public operator fun iterator(): Iterator<T>
}

/**
 * Classes that inherit from this interface can be represented as a sequence of elements that can
 * be iterated over and that supports removing elements during iteration.
 * @param T the type of element being iterated over. The mutable iterator is invariant in its element type.
 */
public interface MutableIterable<out T> : Iterable<T> {
    /**
     * Returns an iterator over the elements of this sequence that supports removing elements during iteration.
     */
    override fun iterator(): MutableIterator<T>
}

/**
 * A generic collection of elements. Methods in this interface support only read-only access to the collection;
 * read/write access is supported through the [MutableCollection] interface.
 * @param E the type of elements contained in the collection. The collection is covariant in its element type.
 */
public interface Collection<out E> : Iterable<E> {
    // Query Operations
    /**
     * Returns the size of the collection.
     */
    public konst size: Int

    /**
     * Returns `true` if the collection is empty (contains no elements), `false` otherwise.
     */
    public fun isEmpty(): Boolean

    /**
     * Checks if the specified element is contained in this collection.
     */
    public operator fun contains(element: @UnsafeVariance E): Boolean

    override fun iterator(): Iterator<E>

    // Bulk Operations
    /**
     * Checks if all elements in the specified collection are contained in this collection.
     */
    public fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
}

/**
 * A generic collection of elements that supports adding and removing elements.
 *
 * @param E the type of elements contained in the collection. The mutable collection is invariant in its element type.
 */
public interface MutableCollection<E> : Collection<E>, MutableIterable<E> {
    // Query Operations
    override fun iterator(): MutableIterator<E>

    // Modification Operations
    /**
     * Adds the specified element to the collection.
     *
     * @return `true` if the element has been added, `false` if the collection does not support duplicates
     * and the element is already contained in the collection.
     */
    public fun add(element: E): Boolean

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present.
     *
     * @return `true` if the element has been successfully removed; `false` if it was not present in the collection.
     */
    public fun remove(element: E): Boolean

    // Bulk Modification Operations
    /**
     * Adds all of the elements of the specified collection to this collection.
     *
     * @return `true` if any of the specified elements was added to the collection, `false` if the collection was not modified.
     */
    public fun addAll(elements: Collection<E>): Boolean

    /**
     * Removes all of this collection's elements that are also contained in the specified collection.
     *
     * @return `true` if any of the specified elements was removed from the collection, `false` if the collection was not modified.
     */
    public fun removeAll(elements: Collection<E>): Boolean

    /**
     * Retains only the elements in this collection that are contained in the specified collection.
     *
     * @return `true` if any element was removed from the collection, `false` if the collection was not modified.
     */
    public fun retainAll(elements: Collection<E>): Boolean

    /**
     * Removes all elements from this collection.
     */
    public fun clear(): Unit
}

/**
 * A generic ordered collection of elements. Methods in this interface support only read-only access to the list;
 * read/write access is supported through the [MutableList] interface.
 * @param E the type of elements contained in the list. The list is covariant in its element type.
 */
public interface List<out E> : Collection<E> {
    // Query Operations

    override konst size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: @UnsafeVariance E): Boolean
    override fun iterator(): Iterator<E>

    // Bulk Operations
    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean

    // Positional Access Operations
    /**
     * Returns the element at the specified index in the list.
     */
    public operator fun get(index: Int): E

    // Search Operations
    /**
     * Returns the index of the first occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    public fun indexOf(element: @UnsafeVariance E): Int

    /**
     * Returns the index of the last occurrence of the specified element in the list, or -1 if the specified
     * element is not contained in the list.
     */
    public fun lastIndexOf(element: @UnsafeVariance E): Int

    // List Iterators
    /**
     * Returns a list iterator over the elements in this list (in proper sequence).
     */
    public fun listIterator(): ListIterator<E>

    /**
     * Returns a list iterator over the elements in this list (in proper sequence), starting at the specified [index].
     */
    public fun listIterator(index: Int): ListIterator<E>

    // View
    /**
     * Returns a view of the portion of this list between the specified [fromIndex] (inclusive) and [toIndex] (exclusive).
     * The returned list is backed by this list, so non-structural changes in the returned list are reflected in this list, and vice-versa.
     *
     * Structural changes in the base list make the behavior of the view undefined.
     */
    public fun subList(fromIndex: Int, toIndex: Int): List<E>
}

/**
 * A generic ordered collection of elements that supports adding and removing elements.
 * @param E the type of elements contained in the list. The mutable list is invariant in its element type.
 */
public interface MutableList<E> : List<E>, MutableCollection<E> {
    // Modification Operations
    /**
     * Adds the specified element to the end of this list.
     *
     * @return `true` because the list is always modified as the result of this operation.
     */
    override fun add(element: E): Boolean

    override fun remove(element: E): Boolean

    // Bulk Modification Operations
    /**
     * Adds all of the elements of the specified collection to the end of this list.
     *
     * The elements are appended in the order they appear in the [elements] collection.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    override fun addAll(elements: Collection<E>): Boolean

    /**
     * Inserts all of the elements of the specified collection [elements] into this list at the specified [index].
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    public fun addAll(index: Int, elements: Collection<E>): Boolean

    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun clear(): Unit

    // Positional Access Operations
    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @return the element previously at the specified position.
     */
    public operator fun set(index: Int, element: E): E

    /**
     * Inserts an element into the list at the specified [index].
     */
    public fun add(index: Int, element: E): Unit

    /**
     * Removes an element at the specified [index] from the list.
     *
     * @return the element that has been removed.
     */
    public fun removeAt(index: Int): E

    // List Iterators
    override fun listIterator(): MutableListIterator<E>

    override fun listIterator(index: Int): MutableListIterator<E>

    // View
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E>
}

/**
 * A generic unordered collection of elements that does not support duplicate elements.
 * Methods in this interface support only read-only access to the set;
 * read/write access is supported through the [MutableSet] interface.
 * @param E the type of elements contained in the set. The set is covariant in its element type.
 */
public interface Set<out E> : Collection<E> {
    // Query Operations

    override konst size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: @UnsafeVariance E): Boolean
    override fun iterator(): Iterator<E>

    // Bulk Operations
    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
}

/**
 * A generic unordered collection of elements that does not support duplicate elements, and supports
 * adding and removing elements.
 * @param E the type of elements contained in the set. The mutable set is invariant in its element type.
 */
public interface MutableSet<E> : Set<E>, MutableCollection<E> {
    // Query Operations
    override fun iterator(): MutableIterator<E>

    // Modification Operations

    /**
     * Adds the specified element to the set.
     *
     * @return `true` if the element has been added, `false` if the element is already contained in the set.
     */
    override fun add(element: E): Boolean

    override fun remove(element: E): Boolean

    // Bulk Modification Operations

    override fun addAll(elements: Collection<E>): Boolean
    override fun removeAll(elements: Collection<E>): Boolean
    override fun retainAll(elements: Collection<E>): Boolean
    override fun clear(): Unit
}

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

    /**
     * Returns the konstue corresponding to the given [key], or [defaultValue] if such a key is not present in the map.
     *
     * @since JDK 1.8
     */
    @SinceKotlin("1.1")
    @PlatformDependent
    public fun getOrDefault(key: K, defaultValue: @UnsafeVariance V): V {
        // See default implementation in JDK sources
        throw NotImplementedError()
    }

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

    /**
     * Removes the entry for the specified key only if it is mapped to the specified konstue.
     *
     * @return true if entry was removed
     */
    @SinceKotlin("1.1")
    @PlatformDependent
    public fun remove(key: K, konstue: V): Boolean {
        // See default implementation in JDK sources
        return true
    }

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
