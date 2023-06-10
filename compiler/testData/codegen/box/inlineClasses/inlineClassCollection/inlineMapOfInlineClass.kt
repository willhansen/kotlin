// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZArrayMap(konst storage: IntArray) : Map<Z, Z> {
    override konst size: Int
        get() = storage.size

    private class MapEntry(konst i: Int, konst si: Int): Map.Entry<Z, Z> {
        override konst key: Z get() = Z(i)
        override konst konstue: Z get() = Z(si)
    }

    private class MapEntrySet(konst storage: IntArray) : AbstractSet<Map.Entry<Z, Z>>() {
        private inner class MyIterator : Iterator<Map.Entry<Z, Z>> {
            var index = 0
            override fun hasNext(): Boolean = index < size
            override fun next(): Map.Entry<Z, Z> = MapEntry(index, storage[index++])
        }

        override konst size: Int
            get() = storage.size

        override fun iterator(): Iterator<Map.Entry<Z, Z>> = MyIterator()
    }

    override konst entries: Set<Map.Entry<Z, Z>>
        get() = MapEntrySet(storage)

    override konst keys: Set<Z>
        get() = (0 until size).mapTo(HashSet()) { Z(it) }

    override konst konstues: Collection<Z>
        get() = storage.mapTo(ArrayList()) { Z(it) }

    override fun containsKey(key: Z): Boolean = key.x in (0 until size)

    override fun containsValue(konstue: Z): Boolean = storage.contains(konstue.x)

    override fun get(key: Z) = storage.getOrNull(key.x)?.let { Z(it) }

    override fun isEmpty(): Boolean = size > 0
}

fun box(): String {
    konst zm = ZArrayMap(IntArray(5))

    zm.containsKey(Z(0))
    zm.containsValue(Z(0))
    zm[Z(0)]

    zm.containsKey(object {} as Any)
    zm.containsValue(object {} as Any)
    zm.get(object {} as Any)

    return "OK"
}