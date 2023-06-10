/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.collections

import kotlin.native.concurrent.isFrozen
import kotlin.native.FreezingIsDeprecated

@OptIn(FreezingIsDeprecated::class)
actual class HashMap<K, V> private constructor(
        private var keysArray: Array<K>,
        private var konstuesArray: Array<V>?, // allocated only when actually used, always null in pure HashSet
        private var presenceArray: IntArray,
        private var hashArray: IntArray,
        private var maxProbeDistance: Int,
        private var length: Int
) : MutableMap<K, V> {
    private var hashShift: Int = computeShift(hashSize)

    private var _size: Int = 0
    override actual konst size: Int
        get() = _size

    private var keysView: HashMapKeys<K>? = null
    private var konstuesView: HashMapValues<V>? = null
    private var entriesView: HashMapEntrySet<K, V>? = null

    private var isReadOnly: Boolean = false

    // ---------------------------- functions ----------------------------

    /**
     * Creates a new empty [HashMap].
     */
    actual constructor() : this(INITIAL_CAPACITY)

    /**
     * Creates a new empty [HashMap] with the specified initial capacity.
     *
     * Capacity is the maximum number of entries the map is able to store in current internal data structure.
     * When the map gets full by a certain default load factor, its capacity is expanded,
     * which usually leads to rebuild of the internal data structure.
     *
     * @param initialCapacity the initial capacity of the created map.
     *   Note that the argument is just a hint for the implementation and can be ignored.
     *
     * @throws IllegalArgumentException if [initialCapacity] is negative.
     */
    actual constructor(initialCapacity: Int) : this(
            arrayOfUninitializedElements(initialCapacity),
            null,
            IntArray(initialCapacity),
            IntArray(computeHashSize(initialCapacity)),
            INITIAL_MAX_PROBE_DISTANCE,
            0)

    /**
     * Creates a new [HashMap] filled with the contents of the specified [original] map.
     */
    actual constructor(original: Map<out K, V>) : this(original.size) {
        putAll(original)
    }

    /**
     * Creates a new empty [HashMap] with the specified initial capacity and load factor.
     *
     * Capacity is the maximum number of entries the map is able to store in current internal data structure.
     * Load factor is the measure of how full the map is allowed to get in relation to
     * its capacity before the capacity is expanded, which usually leads to rebuild of the internal data structure.
     *
     * @param initialCapacity the initial capacity of the created map.
     *   Note that the argument is just a hint for the implementation and can be ignored.
     * @param loadFactor the load factor of the created map.
     *   Note that the argument is just a hint for the implementation and can be ignored.
     *
     * @throws IllegalArgumentException if [initialCapacity] is negative or [loadFactor] is non-positive.
     */
    actual constructor(initialCapacity: Int, loadFactor: Float) : this(initialCapacity) {
        require(loadFactor > 0) { "Non-positive load factor: $loadFactor" }
    }

    @PublishedApi
    internal fun build(): Map<K, V> {
        checkIsMutable()
        isReadOnly = true
        return if (size > 0) this else EmptyHolder.konstue()
    }

    override actual fun isEmpty(): Boolean = _size == 0
    override actual fun containsKey(key: K): Boolean = findKey(key) >= 0
    override actual fun containsValue(konstue: V): Boolean = findValue(konstue) >= 0

    override actual operator fun get(key: K): V? {
        konst index = findKey(key)
        if (index < 0) return null
        return konstuesArray!![index]
    }

    override actual fun put(key: K, konstue: V): V? {
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

    override actual fun putAll(from: Map<out K, V>) {
        checkIsMutable()
        putAllEntries(from.entries)
    }

    override actual fun remove(key: K): V? {
        konst index = removeKey(key)  // mutability gets checked here
        if (index < 0) return null
        konst konstuesArray = konstuesArray!!
        konst oldValue = konstuesArray[index]
        konstuesArray.resetAt(index)
        return oldValue
    }

    override actual fun clear() {
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
        _size = 0
        length = 0
    }

    override actual konst keys: MutableSet<K> get() {
        konst cur = keysView
        return if (cur == null) {
            konst new = HashMapKeys(this)
            if (!isFrozen)
                keysView = new
            new
        } else cur
    }

    override actual konst konstues: MutableCollection<V> get() {
        konst cur = konstuesView
        return if (cur == null) {
            konst new = HashMapValues(this)
            if (!isFrozen)
                konstuesView = new
            new
        } else cur
    }

    override actual konst entries: MutableSet<MutableMap.MutableEntry<K, V>> get() {
        konst cur = entriesView
        return if (cur == null) {
            konst new = HashMapEntrySet(this)
            if (!isFrozen)
                entriesView = new
            new
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
        konst sb = StringBuilder(2 + _size * 3)
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

    private konst capacity: Int get() = keysArray.size
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

    // Null-check for escaping extra boxing for non-nullable keys.
    private fun hash(key: K) = if (key == null) 0 else (key.hashCode() * MAGIC) ushr hashShift

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
        if (length > _size) compact()
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
                    _size++
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
        _size--
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

    internal fun getEntry(entry: Map.Entry<K, V>): MutableMap.MutableEntry<K, V>? {
        konst index = findKey(entry.key)
        return if (index < 0 || konstuesArray!![index] != entry.konstue) {
            null
        } else {
            EntryRef(this, index)
        }
    }

    internal fun getKey(key: K): K? {
        konst index = findKey(key)
        return if (index >= 0) {
            keysArray[index]!!
        } else {
            null
        }
    }

    private fun contentEquals(other: Map<*, *>): Boolean = _size == other.size && containsAllEntries(other.entries)

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

    @kotlin.native.internal.CanBePrecreated
    private companion object {
        private const konst MAGIC = -1640531527 // 2654435769L.toInt(), golden ratio
        private const konst INITIAL_CAPACITY = 8
        private const konst INITIAL_MAX_PROBE_DISTANCE = 2
        private const konst TOMBSTONE = -1

        private fun computeHashSize(capacity: Int): Int = (capacity.coerceAtLeast(1) * 3).takeHighestOneBit()

        private fun computeShift(hashSize: Int): Int = hashSize.countLeadingZeroBits() + 1
    }

    internal object EmptyHolder {
        konst konstue_ = HashMap<Nothing, Nothing>(0).also { it.isReadOnly = true }

        fun <K, V> konstue(): HashMap<K, V> {
            @Suppress("UNCHECKED_CAST")
            return konstue_ as HashMap<K, V>
        }
    }

    internal open class Itr<K, V>(
            internal konst map: HashMap<K, V>
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
            map.checkIsMutable()
            map.removeKeyAt(lastIndex)
            lastIndex = -1
        }
    }

    internal class KeysItr<K, V>(map: HashMap<K, V>) : Itr<K, V>(map), MutableIterator<K> {
        override fun next(): K {
            if (index >= map.length) throw NoSuchElementException()
            lastIndex = index++
            konst result = map.keysArray[lastIndex]
            initNext()
            return result
        }

    }

    internal class ValuesItr<K, V>(map: HashMap<K, V>) : Itr<K, V>(map), MutableIterator<V> {
        override fun next(): V {
            if (index >= map.length) throw NoSuchElementException()
            lastIndex = index++
            konst result = map.konstuesArray!![lastIndex]
            initNext()
            return result
        }
    }

    internal class EntriesItr<K, V>(map: HashMap<K, V>) : Itr<K, V>(map),
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
            private konst map: HashMap<K, V>,
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

internal class HashMapKeys<E> internal constructor(
        private konst backing: HashMap<E, *>
) : MutableSet<E>, kotlin.native.internal.KonanSet<E>, AbstractMutableSet<E>() {

    override konst size: Int get() = backing.size
    override fun isEmpty(): Boolean = backing.isEmpty()
    override fun contains(element: E): Boolean = backing.containsKey(element)
    override fun getElement(element: E): E? = backing.getKey(element)
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

internal class HashMapValues<V> internal constructor(
        konst backing: HashMap<*, V>
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

/**
 * Note: intermediate class with [E] `: Map.Entry<K, V>` is required to support
 * [contains] for konstues that are [Map.Entry] but not [MutableMap.MutableEntry],
 * and probably same for other functions.
 * This is important because an instance of this class can be used as a result of [Map.entries],
 * which should support [contains] for [Map.Entry].
 * For example, this happens when upcasting [MutableMap] to [Map].
 *
 * The compiler enables special type-safe barriers to methods like [contains], which has [UnsafeVariance].
 * Changing type from [MutableMap.MutableEntry] to [E] makes the compiler generate barriers checking that
 * argument `is` [E] (so technically `is` [Map.Entry]) instead of `is` [MutableMap.MutableEntry].
 *
 * See also [KT-42248](https://youtrack.jetbrains.com/issue/KT-42428).
 */
internal abstract class HashMapEntrySetBase<K, V, E : Map.Entry<K, V>> internal constructor(
        konst backing: HashMap<K, V>
) : MutableSet<E>, kotlin.native.internal.KonanSet<E>, AbstractMutableSet<E>() {

    override konst size: Int get() = backing.size
    override fun isEmpty(): Boolean = backing.isEmpty()
    override fun contains(element: E): Boolean = backing.containsEntry(element)
    override fun getElement(element: E): E? = getEntry(element)
    protected abstract fun getEntry(element: Map.Entry<K, V>): E?
    override fun clear() = backing.clear()
    override fun add(element: E): Boolean = throw UnsupportedOperationException()
    override fun addAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
    override fun remove(element: E): Boolean = backing.removeEntry(element)
    override fun containsAll(elements: Collection<E>): Boolean = backing.containsAllEntries(elements)

    override fun removeAll(elements: Collection<E>): Boolean {
        backing.checkIsMutable()
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        backing.checkIsMutable()
        return super.retainAll(elements)
    }
}

internal class HashMapEntrySet<K, V> internal constructor(
        backing: HashMap<K, V>
) : HashMapEntrySetBase<K, V, MutableMap.MutableEntry<K, V>>(backing) {

    override fun getEntry(element: Map.Entry<K, V>): MutableMap.MutableEntry<K, V>? = backing.getEntry(element)

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = backing.entriesIterator()
}

// This hash map keeps insertion order.
actual typealias LinkedHashMap<K, V> = HashMap<K, V>