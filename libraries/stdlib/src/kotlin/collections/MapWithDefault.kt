/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("MapsKt")

package kotlin.collections

/**
 * Returns the konstue for the given key, or the implicit default konstue for this map.
 * By default no implicit konstue is provided for maps and a [NoSuchElementException] is thrown.
 * To create a map with implicit default konstue use [withDefault] method.
 *
 * @throws NoSuchElementException when the map doesn't contain a konstue for the specified key and no implicit default was provided for that map.
 */
@kotlin.jvm.JvmName("getOrImplicitDefaultNullable")
@PublishedApi
internal fun <K, V> Map<K, V>.getOrImplicitDefault(key: K): V {
    if (this is MapWithDefault)
        return this.getOrImplicitDefault(key)

    return getOrElseNullable(key, { throw NoSuchElementException("Key $key is missing in the map.") })
}

/**
 * Returns a wrapper of this read-only map, having the implicit default konstue provided with the specified function [defaultValue].
 *
 * This implicit default konstue is used when the original map doesn't contain a konstue for the key specified
 * and a konstue is obtained with [Map.getValue] function, for example when properties are delegated to the map.
 *
 * When this map already has an implicit default konstue provided with a former call to [withDefault], it is being replaced by this call.
 */
public fun <K, V> Map<K, V>.withDefault(defaultValue: (key: K) -> V): Map<K, V> =
    when (this) {
        is MapWithDefault -> this.map.withDefault(defaultValue)
        else -> MapWithDefaultImpl(this, defaultValue)
    }

/**
 * Returns a wrapper of this mutable map, having the implicit default konstue provided with the specified function [defaultValue].
 *
 * This implicit default konstue is used when the original map doesn't contain a konstue for the key specified
 * and a konstue is obtained with [Map.getValue] function, for example when properties are delegated to the map.
 *
 * When this map already has an implicit default konstue provided with a former call to [withDefault], it is being replaced by this call.
 */
@kotlin.jvm.JvmName("withDefaultMutable")
public fun <K, V> MutableMap<K, V>.withDefault(defaultValue: (key: K) -> V): MutableMap<K, V> =
    when (this) {
        is MutableMapWithDefault -> this.map.withDefault(defaultValue)
        else -> MutableMapWithDefaultImpl(this, defaultValue)
    }


private interface MapWithDefault<K, out V> : Map<K, V> {
    public konst map: Map<K, V>
    public fun getOrImplicitDefault(key: K): V
}

private interface MutableMapWithDefault<K, V> : MutableMap<K, V>, MapWithDefault<K, V> {
    public override konst map: MutableMap<K, V>
}


private class MapWithDefaultImpl<K, out V>(public override konst map: Map<K, V>, private konst default: (key: K) -> V) : MapWithDefault<K, V> {
    override fun equals(other: Any?): Boolean = map.equals(other)
    override fun hashCode(): Int = map.hashCode()
    override fun toString(): String = map.toString()
    override konst size: Int get() = map.size
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun containsKey(key: K): Boolean = map.containsKey(key)
    override fun containsValue(konstue: @UnsafeVariance V): Boolean = map.containsValue(konstue)
    override fun get(key: K): V? = map.get(key)
    override konst keys: Set<K> get() = map.keys
    override konst konstues: Collection<V> get() = map.konstues
    override konst entries: Set<Map.Entry<K, V>> get() = map.entries

    override fun getOrImplicitDefault(key: K): V = map.getOrElseNullable(key, { default(key) })
}

private class MutableMapWithDefaultImpl<K, V>(public override konst map: MutableMap<K, V>, private konst default: (key: K) -> V) : MutableMapWithDefault<K, V> {
    override fun equals(other: Any?): Boolean = map.equals(other)
    override fun hashCode(): Int = map.hashCode()
    override fun toString(): String = map.toString()
    override konst size: Int get() = map.size
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun containsKey(key: K): Boolean = map.containsKey(key)
    override fun containsValue(konstue: @UnsafeVariance V): Boolean = map.containsValue(konstue)
    override fun get(key: K): V? = map.get(key)
    override konst keys: MutableSet<K> get() = map.keys
    override konst konstues: MutableCollection<V> get() = map.konstues
    override konst entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries

    override fun put(key: K, konstue: V): V? = map.put(key, konstue)
    override fun remove(key: K): V? = map.remove(key)
    override fun putAll(from: Map<out K, V>) = map.putAll(from)
    override fun clear() = map.clear()

    override fun getOrImplicitDefault(key: K): V = map.getOrElseNullable(key, { default(key) })
}

