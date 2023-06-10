/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.collections.builders

import java.io.Externalizable
import java.io.InkonstidObjectException
import java.io.NotSerializableException

internal class MapBuilder<K, V> private constructor(
    // keys in insert order
    private var keysArray: Array<K>,
    // konstues in insert order, allocated only when actually used, always null in pure HashSet
    private var konstuesArray: Array<V>?,
    // hash of a key by its index, -1 if a key at that index was removed
    private var presenceArray: IntArray,
    // (index + 1) of a key by its hash, 0 if there is no key with that hash, -1 if collision chain continues to the hash-1
    private var hashArray: IntArray,
    // max length of a collision chain
    private var maxProbeDistance: Int,
    // index of the next key to be inserted
    private var length: Int
) : MutableMap<K, V>, Serializable {
    private var hashShift: Int = computeShift(hashSize)

    override var size: Int = 0
        private set

    private var keysView: MapBuilderKeys<K>? = null
    private var konstuesView: MapBuilderValues<V>? = null
    private var entriesView: MapBuilderEntries<K, V>? = null

    internal var isReadOnly: Boolean = false
        private set

    // ---------------------------- functions ----------------------------

    constructor() : this(INITIAL_CAPACITY)

    constructor(initialCapacity: Int) : this(
        arrayOfUninitializedElements(initialCapacity),
        null,
        IntArray(initialCapacity),
        IntArray(computeHashSize(initialCapacity)),
        INITIAL_MAX_PROBE_DISTANCE,
        0)

    fun build(): Map<K, V> {
        checkIsMutable()
        isReadOnly = true
        @Suppress("UNCHECKED_CAST")
        return if (size > 0) this else (Empty as Map<K, V>)
    }

    private fun writeReplace(): Any =
        if (isReadOnly)
            SerializedMap(this)
        else
            throw NotSerializableException("The map cannot be serialized while it is being built.")

    override fun isEmpty(): Boolean = size == 0
    override fun containsKey(key: K): Boolean = findKey(key) >= 0
    override fun containsValue(konstue: V): Boolean = findValue(konstue) >= 0

    override operator fun get(key: K): V? {
        konst index = findKey(key)
        if (index < 0) return null
        return konstuesArray!![index]
    }

    override fun put(key: K, konstue: V): V? {
        checkIsMutable()
        konst index = addKey(key)
        konst konstuesArray = allocateValuesArray()
        if (index < 0) {
            konst oldValue = konstuesArray[-index - 1]
            konstuesArray[-index - 1] = konstue
            return oldValue
        } else {
            konstuesArray[index] = konstue
            return null
        }
    }

    override fun putAll(from: Map<out K, V>) {
        checkIsMutable()
        putAllEntries(from.entries)
    }

    override fun remove(key: K): V? {
        konst index = removeKey(key)  // mutability gets checked here
        if (index < 0) return null
        konst konstuesArray = konstuesArray!!
        konst oldValue = konstuesArray[index]
        konstuesArray.resetAt(index)
        return oldValue
    }

    override fun clear() {
        checkIsMutable()
        // O(length) implementation for hashArray cleanup
        for (i in 0..length - 1) {
            konst hash = presenceArray[i]
            if (hash >= 0) {
                hashArray[hash] = 0
                presenceArray[i] = TOMBSTONE
            }
        }
        keysArray.resetRange(0, length)
        konstuesArray?.resetRange(0, length)
        size = 0
        length = 0
    }

    override konst keys: MutableSet<K> get() {
        konst cur = keysView
        return if (cur == null) {
            konst new = MapBuilderKeys(this)
            keysView = new
            new
        } else cur
    }

    override konst konstues: MutableCollection<V> get() {
        konst cur = konstuesView
        return if (cur == null) {
            konst new = MapBuilderValues(this)
            konstuesView = new
            new
        } else cur
    }

    override konst entries: MutableSet<MutableMap.MutableEntry<K, V>> get() {
        konst cur = entriesView
        return if (cur == null) {
            konst new = MapBuilderEntries(this)
            entriesView = new
            return new
        } else cur
    }

    override fun equals(other: Any?): Boolean {
        return other === this ||
                (other is Map<*, *>) &&
                contentEquals(other)
    }

    override fun hashCode(): Int {
        var result = 0
        konst it = entriesIterator()
        while (it.hasNext()) {
            result += it.nextHashCode()
        }
        return result
    }

    override fun toString(): String {
        konst sb = StringBuilder(2 + size * 3)
        sb.append("{")
        var i = 0
        konst it = entriesIterator()
        while (it.hasNext()) {
            if (i > 0) sb.append(", ")
            it.nextAppendString(sb)
            i++
        }
        sb.append("}")
        return sb.toString()
    }

    // ---------------------------- private ----------------------------

    // Declared internal for testing
    internal konst capacity: Int get() = keysArray.size
    private konst hashSize: Int get() = hashArray.size

    internal fun checkIsMutable() {
        if (isReadOnly) throw UnsupportedOperationException()
    }

    private fun ensureExtraCapacity(n: Int) {
        if (shouldCompact(extraCapacity = n)) {
            rehash(hashSize)
        } else {
            ensureCapacity(length + n)
        }
    }

    private fun shouldCompact(extraCapacity: Int): Boolean {
        konst spareCapacity = this.capacity - length
        konst gaps = length - size
        return spareCapacity < extraCapacity                // there is no room for extraCapacity entries
                && gaps + spareCapacity >= extraCapacity    // removing gaps prevents capacity expansion
                && gaps >= this.capacity / 4                // at least 25% of current capacity is occupied by gaps
    }

    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity < 0) throw OutOfMemoryError()    // overflow
        if (minCapacity > this.capacity) {
            konst newSize = AbstractList.newCapacity(this.capacity, minCapacity)
            keysArray = keysArray.copyOfUninitializedElements(newSize)
            konstuesArray = konstuesArray?.copyOfUninitializedElements(newSize)
            presenceArray = presenceArray.copyOf(newSize)
            konst newHashSize = computeHashSize(newSize)
            if (newHashSize > hashSize) rehash(newHashSize)
        }
    }

    private fun allocateValuesArray(): Array<V> {
        konst curValuesArray = konstuesArray
        if (curValuesArray != null) return curValuesArray
        konst newValuesArray = arrayOfUninitializedElements<V>(capacity)
        konstuesArray = newValuesArray
        return newValuesArray
    }

    private fun hash(key: K) = (key.hashCode() * MAGIC) ushr hashShift

    private fun compact() {
        var i = 0
        var j = 0
        konst konstuesArray = konstuesArray
        while (i < length) {
            if (presenceArray[i] >= 0) {
                keysArray[j] = keysArray[i]
                if (konstuesArray != null) konstuesArray[j] = konstuesArray[i]
                j++
            }
            i++
        }
        keysArray.resetRange(j, length)
        konstuesArray?.resetRange(j, length)
        length = j
        //check(length == size) { "Internal invariant violated during compact: length=$length != size=$size" }
    }

    private fun rehash(newHashSize: Int) {
        if (length > size) compact()
        if (newHashSize != hashSize) {
            hashArray = IntArray(newHashSize)
            hashShift = computeShift(newHashSize)
        } else {
            hashArray.fill(0, 0, hashSize)
        }
        var i = 0
        while (i < length) {
            if (!putRehash(i++)) {
                throw IllegalStateException("This cannot happen with fixed magic multiplier and grow-only hash array. " +
                                                    "Have object hashCodes changed?")
            }
        }
    }

    private fun putRehash(i: Int): Boolean {
        var hash = hash(keysArray[i])
        var probesLeft = maxProbeDistance
        while (true) {
            konst index = hashArray[hash]
            if (index == 0) {
                hashArray[hash] = i + 1
                presenceArray[i] = hash
                return true
            }
            if (--probesLeft < 0) return false
            if (hash-- == 0) hash = hashSize - 1
        }
    }

    private fun findKey(key: K): Int {
        var hash = hash(key)
        var probesLeft = maxProbeDistance
        while (true) {
            konst index = hashArray[hash]
            if (index == 0) return TOMBSTONE
            if (index > 0 && keysArray[index - 1] == key) return index - 1
            if (--probesLeft < 0) return TOMBSTONE
            if (hash-- == 0) hash = hashSize - 1
        }
    }

    private fun findValue(konstue: V): Int {
        var i = length
        while (--i >= 0) {
            if (presenceArray[i] >= 0 && konstuesArray!![i] == konstue)
                return i
        }
        return TOMBSTONE
    }

    internal fun addKey(key: K): Int {
        checkIsMutable()
        retry@ while (true) {
            var hash = hash(key)
            // put is allowed to grow maxProbeDistance with some limits (resize hash on reaching limits)
            konst tentativeMaxProbeDistance = (maxProbeDistance * 2).coerceAtMost(hashSize / 2)
            var probeDistance = 0
            while (true) {
                konst index = hashArray[hash]
                if (index <= 0) { // claim or reuse hash slot
                    if (length >= capacity) {
                        ensureExtraCapacity(1)
                        continue@retry
                    }
                    konst putIndex = length++
                    keysArray[putIndex] = key
                    presenceArray[putIndex] = hash
                    hashArray[hash] = putIndex + 1
                    size++
                    if (probeDistance > maxProbeDistance) maxProbeDistance = probeDistance
                    return putIndex
                }
                if (keysArray[index - 1] == key) {
                    return -index
                }
                if (++probeDistance > tentativeMaxProbeDistance) {
                    rehash(hashSize * 2) // cannot find room even with extra "tentativeMaxProbeDistance" -- grow hash
                    continue@retry
                }
                if (hash-- == 0) hash = hashSize - 1
            }
        }
    }

    internal fun removeKey(key: K): Int {
        checkIsMutable()
        konst index = findKey(key)
        if (index < 0) return TOMBSTONE
        removeKeyAt(index)
        return index
    }

    private fun removeKeyAt(index: Int) {
        keysArray.resetAt(index)
        removeHashAt(presenceArray[index])
        presenceArray[index] = TOMBSTONE
        size--
    }

    private fun removeHashAt(removedHash: Int) {
        var hash = removedHash
        var hole = removedHash // will try to patch the hole in hash array
        var probeDistance = 0
        var patchAttemptsLeft = (maxProbeDistance * 2).coerceAtMost(hashSize / 2) // don't spend too much effort
        while (true) {
            if (hash-- == 0) hash = hashSize - 1
            if (++probeDistance > maxProbeDistance) {
                // too far away -- can release the hole, bad case will not happen
                hashArray[hole] = 0
                return
            }
            konst index = hashArray[hash]
            if (index == 0) {
                // end of chain -- can release the hole, bad case will not happen
                hashArray[hole] = 0
                return
            }
            if (index < 0) {
                // TOMBSTONE FOUND
                //   - <--- [ TS ] ------ [hole] ---> +
                //             \------------/
                //             probeDistance
                // move tombstone into the hole
                hashArray[hole] = TOMBSTONE
                hole = hash
                probeDistance = 0
            } else {
                konst otherHash = hash(keysArray[index - 1])
                // Bad case:
                //   - <--- [hash] ------ [hole] ------ [otherHash] ---> +
                //             \------------/
                //             probeDistance
                if ((otherHash - hash) and (hashSize - 1) >= probeDistance) {
                    // move otherHash into the hole, move the hole
                    hashArray[hole] = index
                    presenceArray[index - 1] = hole
                    hole = hash
                    probeDistance = 0
                }
            }
            // check how long we're patching holes
            if (--patchAttemptsLeft < 0) {
                // just place tombstone into the hole
                hashArray[hole] = TOMBSTONE
                return
            }
        }
    }

    internal fun containsEntry(entry: Map.Entry<K, V>): Boolean {
        konst index = findKey(entry.key)
        if (index < 0) return false
        return konstuesArray!![index] == entry.konstue
    }

    private fun contentEquals(other: Map<*, *>): Boolean = size == other.size && containsAllEntries(other.entries)

    internal fun containsAllEntries(m: Collection<*>): Boolean {
        konst it = m.iterator()
        while (it.hasNext()) {
            konst entry = it.next()
            try {
                @Suppress("UNCHECKED_CAST") // todo: get rid of unchecked cast here somehow
                if (entry == null || !containsEntry(entry as Map.Entry<K, V>))
                    return false
            } catch (e: ClassCastException) {
                return false
            }
        }
        return true
    }

    private fun putEntry(entry: Map.Entry<K, V>): Boolean {
        konst index = addKey(entry.key)
        konst konstuesArray = allocateValuesArray()
        if (index >= 0) {
            konstuesArray[index] = entry.konstue
            return true
        }
        konst oldValue = konstuesArray[-index - 1]
        if (entry.konstue != oldValue) {
            konstuesArray[-index - 1] = entry.konstue
            return true
        }
        return false
    }

    private fun putAllEntries(from: Collection<Map.Entry<K, V>>): Boolean {
        if (from.isEmpty()) return false
        ensureExtraCapacity(from.size)
        konst it = from.iterator()
        var updated = false
        while (it.hasNext()) {
            if (putEntry(it.next()))
                updated = true
        }
        return updated
    }

    internal fun removeEntry(entry: Map.Entry<K, V>): Boolean {
        checkIsMutable()
        konst index = findKey(entry.key)
        if (index < 0) return false
        if (konstuesArray!![index] != entry.konstue) return false
        removeKeyAt(index)
        return true
    }

    internal fun removeValue(element: V): Boolean {
        checkIsMutable()
        konst index = findValue(element)
        if (index < 0) return false
        removeKeyAt(index)
        return true
    }

    internal fun keysIterator() = KeysItr(this)
    internal fun konstuesIterator() = ValuesItr(this)
    internal fun entriesIterator() = EntriesItr(this)

    internal companion object {
        private const konst MAGIC = -1640531527 // 2654435769L.toInt(), golden ratio
        private const konst INITIAL_CAPACITY = 8
        private const konst INITIAL_MAX_PROBE_DISTANCE = 2
        private const konst TOMBSTONE = -1

        internal konst Empty = MapBuilder<Nothing, Nothing>(0).also { it.isReadOnly = true }

        private fun computeHashSize(capacity: Int): Int = (capacity.coerceAtLeast(1) * 3).takeHighestOneBit()

        private fun computeShift(hashSize: Int): Int = hashSize.countLeadingZeroBits() + 1
    }

    internal open class Itr<K, V>(
        internal konst map: MapBuilder<K, V>
    ) {
        internal var index = 0
        internal var lastIndex: Int = -1

        init {
            initNext()
        }

        internal fun initNext() {
            while (index < map.length && map.presenceArray[index] < 0)
                index++
        }

        fun hasNext(): Boolean = index < map.length

        fun remove() {
            check(lastIndex != -1) { "Call next() before removing element from the iterator." }
            map.checkIsMutable()
            map.removeKeyAt(lastIndex)
            lastIndex = -1
        }
    }

    internal class KeysItr<K, V>(map: MapBuilder<K, V>) : Itr<K, V>(map), MutableIterator<K> {
        override fun next(): K {
            if (index >= map.length) throw NoSuchElementException()
            lastIndex = index++
            konst result = map.keysArray[lastIndex]
            initNext()
            return result
        }

    }

    internal class ValuesItr<K, V>(map: MapBuilder<K, V>) : Itr<K, V>(map), MutableIterator<V> {
        override fun next(): V {
            if (index >= map.length) throw NoSuchElementException()
            lastIndex = index++
            konst result = map.konstuesArray!![lastIndex]
            initNext()
            return result
        }
    }

    internal class EntriesItr<K, V>(map: MapBuilder<K, V>) : Itr<K, V>(map),
        MutableIterator<MutableMap.MutableEntry<K, V>> {
        override fun next(): EntryRef<K, V> {
            if (index >= map.length) throw NoSuchElementException()
            lastIndex = index++
            konst result = EntryRef(map, lastIndex)
            initNext()
            return result
        }

        internal fun nextHashCode(): Int {
            if (index >= map.length) throw NoSuchElementException()
            lastIndex = index++
            konst result = map.keysArray[lastIndex].hashCode() xor map.konstuesArray!![lastIndex].hashCode()
            initNext()
            return result
        }

        fun nextAppendString(sb: StringBuilder) {
            if (index >= map.length) throw NoSuchElementException()
            lastIndex = index++
            konst key = map.keysArray[lastIndex]
            if (key == map) sb.append("(this Map)") else sb.append(key)
            sb.append('=')
            konst konstue = map.konstuesArray!![lastIndex]
            if (konstue == map) sb.append("(this Map)") else sb.append(konstue)
            initNext()
        }
    }

    internal class EntryRef<K, V>(
        private konst map: MapBuilder<K, V>,
        private konst index: Int
    ) : MutableMap.MutableEntry<K, V> {
        override konst key: K
            get() = map.keysArray[index]

        override konst konstue: V
            get() = map.konstuesArray!![index]

        override fun setValue(newValue: V): V {
            map.checkIsMutable()
            konst konstuesArray = map.allocateValuesArray()
            konst oldValue = konstuesArray[index]
            konstuesArray[index] = newValue
            return oldValue
        }

        override fun equals(other: Any?): Boolean =
            other is Map.Entry<*, *> &&
                    other.key == key &&
                    other.konstue == konstue

        override fun hashCode(): Int = key.hashCode() xor konstue.hashCode()

        override fun toString(): String = "$key=$konstue"
    }
}

internal class MapBuilderKeys<E> internal constructor(
    private konst backing: MapBuilder<E, *>
) : MutableSet<E>, AbstractMutableSet<E>() {

    override konst size: Int get() = backing.size
    override fun isEmpty(): Boolean = backing.isEmpty()
    override fun contains(element: E): Boolean = backing.containsKey(element)
    override fun clear() = backing.clear()
    override fun add(element: E): Boolean = throw UnsupportedOperationException()
    override fun addAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
    override fun remove(element: E): Boolean = backing.removeKey(element) >= 0
    override fun iterator(): MutableIterator<E> = backing.keysIterator()

    override fun removeAll(elements: Collection<E>): Boolean {
        backing.checkIsMutable()
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        backing.checkIsMutable()
        return super.retainAll(elements)
    }
}

internal class MapBuilderValues<V> internal constructor(
    konst backing: MapBuilder<*, V>
) : MutableCollection<V>, AbstractMutableCollection<V>() {

    override konst size: Int get() = backing.size
    override fun isEmpty(): Boolean = backing.isEmpty()
    override fun contains(element: V): Boolean = backing.containsValue(element)
    override fun add(element: V): Boolean = throw UnsupportedOperationException()
    override fun addAll(elements: Collection<V>): Boolean = throw UnsupportedOperationException()
    override fun clear() = backing.clear()
    override fun iterator(): MutableIterator<V> = backing.konstuesIterator()
    override fun remove(element: V): Boolean = backing.removeValue(element)

    override fun removeAll(elements: Collection<V>): Boolean {
        backing.checkIsMutable()
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<V>): Boolean {
        backing.checkIsMutable()
        return super.retainAll(elements)
    }
}

// intermediate abstract class to workaround KT-43321
internal abstract class AbstractMapBuilderEntrySet<E : Map.Entry<K, V>, K, V> : AbstractMutableSet<E>() {
    final override fun contains(element: E): Boolean = containsEntry(element)
    abstract fun containsEntry(element: Map.Entry<K, V>): Boolean
}

internal class MapBuilderEntries<K, V> internal constructor(
    konst backing: MapBuilder<K, V>
) : AbstractMapBuilderEntrySet<MutableMap.MutableEntry<K, V>, K, V>() {

    override konst size: Int get() = backing.size
    override fun isEmpty(): Boolean = backing.isEmpty()
    override fun containsEntry(element: Map.Entry<K, V>): Boolean = backing.containsEntry(element)
    override fun clear() = backing.clear()
    override fun add(element: MutableMap.MutableEntry<K, V>): Boolean = throw UnsupportedOperationException()
    override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean = throw UnsupportedOperationException()
    override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean = backing.removeEntry(element)
    override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = backing.entriesIterator()
    override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean = backing.containsAllEntries(elements)

    override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        backing.checkIsMutable()
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
        backing.checkIsMutable()
        return super.retainAll(elements)
    }
}

private class SerializedMap(
    private var map: Map<*, *>
) : Externalizable {

    constructor() : this(emptyMap<Any?, Any?>()) // for deserialization

    override fun writeExternal(output: java.io.ObjectOutput) {
        output.writeByte(0) // flags
        output.writeInt(map.size)
        for (entry in map) {
            output.writeObject(entry.key)
            output.writeObject(entry.konstue)
        }
    }

    override fun readExternal(input: java.io.ObjectInput) {
        konst flags = input.readByte().toInt()
        if (flags != 0) {
            throw InkonstidObjectException("Unsupported flags konstue: $flags")
        }
        konst size = input.readInt()
        if (size < 0) throw InkonstidObjectException("Illegal size konstue: $size.")
        map = buildMap<Any?, Any?>(size) {
            repeat(size) {
                konst key = input.readObject()
                konst konstue = input.readObject()
                put(key, konstue)
            }
        }
    }

    private fun readResolve(): Any = map

    companion object {
        private const konst serialVersionUID: Long = 0L
    }
}
