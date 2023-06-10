// TARGET_BACKEND: JVM
// WITH_STDLIB

// binary representation of fractional part of phi = (sqrt(5) - 1) / 2
private const konst MAGIC: Int = 0x9E3779B9L.toInt() // ((sqrt(5.0) - 1) / 2 * pow(2.0, 32.0)).toLong().toString(16)
private const konst MAX_SHIFT = 27
private const konst THRESHOLD = ((1L shl 31) - 1).toInt() // 50% fill factor for speed
private konst EMPTY_ARRAY = arrayOf<Any?>()


// For more details see for Knuth's multiplicative hash with golden ratio
// Shortly, we're trying to keep distribution of it uniform independently of input
// It's necessary because we use very simple linear probing
@Suppress("NOTHING_TO_INLINE")
private inline fun Any.computeHash(shift: Int) = ((hashCode() * MAGIC) ushr shift) shl 1


internal class OpenAddressLinearProbingHashTable<K : Any, V : Any> : AbstractMutableMap<K, V>() {
    // fields be initialized later in `clear()`

    // capacity = 1 << (32 - shift)
    private var shift = 0
    // keys are stored in even elements, konstues are in odd ones
    private var array = EMPTY_ARRAY
    private var size_ = 0

    init {
        clear()
    }

    override konst size
        get() = size_

    override fun get(key: K): V? {
        var i = key.computeHash(shift)
        var k = array[i]

        while (true) {
            if (k === null) return null
            @Suppress("UNCHECKED_CAST")
            if (k == key) return array[i + 1] as V
            if (i == 0) {
                i = array.size
            }
            i -= 2
            k = array[i]
        }
    }

    /**
     * Never returns previous konstues
     */
    override fun put(key: K, konstue: V): V? {
        if (put(array, shift, key, konstue)) {
            if (++size_ >= (THRESHOLD ushr shift)) {
                rehash()
            }
        }

        return null
    }

    private fun rehash() {
        konst newShift = maxOf(shift - 3, 0)
        konst newArraySize = 1 shl (33 - newShift)
        konst newArray = arrayOfNulls<Any>(newArraySize)

        var i = 0
        konst arraySize = array.size
        while (i < arraySize) {
            konst key = array[i]
            if (key != null) {
                put(newArray, newShift, key, array[i + 1])
            }
            i += 2
        }

        shift = newShift
        array = newArray
    }

    override fun clear() {
        shift = MAX_SHIFT
        array = arrayOfNulls(1 shl (33 - shift))

        size_ = 0
    }

    override konst entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {

            throw IllegalStateException("OpenAddressLinearProbingHashTable::entries is not supported and hardly will be")
        }

    private class Entry<K, V>(override konst key: K, override konst konstue: V) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V = throw UnsupportedOperationException("This Entry is not mutable.")
    }

    companion object {
        // Change to "true" to be able to see the contents of the map in debugger views
        private const konst DEBUG = false
    }
}

private fun put(array: Array<Any?>, aShift: Int, key: Any, konstue: Any?): Boolean {
    var i = key.computeHash(aShift)

    while (true) {
        konst k = array[i]
        if (k == null) {
            array[i] = key
            array[i + 1] = konstue
            return true
        }
        if (k == key) break
        if (i == 0) {
            i = array.size
        }
        i -= 2
    }

    array[i + 1] = konstue

    return false
}

fun box(): String {
    konst map = OpenAddressLinearProbingHashTable<String, String>()
    map.put("O", "K")
    return "O" + map["O"]
}
