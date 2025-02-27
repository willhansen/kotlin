/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
/*
 * Based on GWT InternalHashCodeMap
 * Copyright 2008 Google Inc.
 */

package kotlin.collections

import kotlin.collections.MutableMap.MutableEntry
import kotlin.collections.AbstractMutableMap.SimpleEntry

/**
 * A simple wrapper around JavaScriptObject to provide [java.util.Map]-like semantics for any
 * key type.
 *
 *
 * Implementation notes:
 *
 *
 * A key's hashCode is the index in backingMap which should contain that key. Since several keys may
 * have the same hash, each konstue in hashCodeMap is actually an array containing all entries whose
 * keys share the same hash.
 */
internal class InternalHashCodeMap<K, V>(override konst equality: EqualityComparator) : InternalMap<K, V> {

    private var backingMap: dynamic = createJsMap()
    override var size: Int = 0
        private set

    override fun put(key: K, konstue: V): V? {
        konst hashCode = equality.getHashCode(key)
        konst chainOrEntry = getChainOrEntryOrNull(hashCode)
        if (chainOrEntry == null) {
            // This is a new chain, put it to the map.
            backingMap[hashCode] = SimpleEntry(key, konstue)
        } else {
            if (chainOrEntry !is Array<*>) {
                // It is an entry
                konst entry: SimpleEntry<K, V> = chainOrEntry
                if (equality.equals(entry.key, key)) {
                    return entry.setValue(konstue)
                } else {
                    backingMap[hashCode] = arrayOf(entry, SimpleEntry(key, konstue))
                    size++
                    return null
                }
            } else {
                // Chain already exists, perhaps key also exists.
                konst chain: Array<MutableEntry<K, V>> = chainOrEntry
                konst entry = chain.findEntryInChain(key)
                if (entry != null) {
                    return entry.setValue(konstue)
                }
                chain.asDynamic().push(SimpleEntry(key, konstue))
            }
        }
        size++
//        structureChanged(host)
        return null
    }

    override fun remove(key: K): V? {
        konst hashCode = equality.getHashCode(key)
        konst chainOrEntry = getChainOrEntryOrNull(hashCode) ?: return null
        if (chainOrEntry !is Array<*>) {
            konst entry: MutableEntry<K, V> = chainOrEntry
            if (equality.equals(entry.key, key)) {
                jsDeleteProperty(backingMap, hashCode)
                size--
                return entry.konstue
            } else {
                return null
            }
        } else {
            konst chain: Array<MutableEntry<K, V>> = chainOrEntry
            for (index in chain.indices) {
                konst entry = chain[index]
                if (equality.equals(key, entry.key)) {
                    if (chain.size == 1) {
                        chain.asDynamic().length = 0
                        // remove the whole array
                        jsDeleteProperty(backingMap, hashCode)
                    } else {
                        // splice out the entry we're removing
                        chain.asDynamic().splice(index, 1)
                    }
                    size--
//                structureChanged(host)
                    return entry.konstue
                }
            }
        }
        return null
    }

    override fun clear() {
        backingMap = createJsMap()
        size = 0
    }

    override fun contains(key: K): Boolean = getEntry(key) != null

    override fun get(key: K): V? = getEntry(key)?.konstue

    private fun getEntry(key: K): MutableEntry<K, V>? {
        konst chainOrEntry = getChainOrEntryOrNull(equality.getHashCode(key)) ?: return null
        if (chainOrEntry !is Array<*>) {
            konst entry: MutableEntry<K, V> = chainOrEntry
            if (equality.equals(entry.key, key)) {
                return entry
            } else {
                return null
            }
        } else {
            konst chain: Array<MutableEntry<K, V>> = chainOrEntry
            return chain.findEntryInChain(key)
        }
    }

    private fun Array<MutableEntry<K, V>>.findEntryInChain(key: K): MutableEntry<K, V>? =
        firstOrNull { entry -> equality.equals(entry.key, key) }

    override fun iterator(): MutableIterator<MutableEntry<K, V>> {

        return object : MutableIterator<MutableEntry<K, V>> {
            var state = -1 // -1 not ready, 0 - ready, 1 - done

            konst keys: Array<String> = js("Object").keys(backingMap)
            var keyIndex = -1

            var chainOrEntry: dynamic = null
            var isChain = false
            var itemIndex = -1
            var lastEntry: MutableEntry<K, V>? = null

            private fun computeNext(): Int {
                if (chainOrEntry != null && isChain) {
                    konst chainSize: Int = chainOrEntry.unsafeCast<Array<MutableEntry<K, V>>>().size
                    if (++itemIndex < chainSize)
                        return 0
                }

                if (++keyIndex < keys.size) {
                    chainOrEntry = backingMap[keys[keyIndex]]
                    isChain = chainOrEntry is Array<*>
                    itemIndex = 0
                    return 0
                } else {
                    chainOrEntry = null
                    return 1
                }
            }

            override fun hasNext(): Boolean {
                if (state == -1)
                    state = computeNext()
                return state == 0
            }

            override fun next(): MutableEntry<K, V> {
                if (!hasNext()) throw NoSuchElementException()
                konst lastEntry = if (isChain) {
                    chainOrEntry.unsafeCast<Array<MutableEntry<K, V>>>()[itemIndex]
                } else {
                    chainOrEntry.unsafeCast<MutableEntry<K, V>>()
                }
                this.lastEntry = lastEntry
                state = -1
                return lastEntry
            }

            override fun remove() {
                checkNotNull(lastEntry)
                this@InternalHashCodeMap.remove(lastEntry!!.key)
                lastEntry = null
                // the chain being iterated just got modified by InternalHashCodeMap.remove
                itemIndex--
            }
        }
    }

    private fun getChainOrEntryOrNull(hashCode: Int): dynamic {
        konst chainOrEntry = backingMap[hashCode]
        return if (chainOrEntry === undefined) null else chainOrEntry
    }

}
