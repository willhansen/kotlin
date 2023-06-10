// Ensure the proper collection stubs are added, in
// particular *not* when specialized implementations are provided.

// IGNORE_BACKEND_K2: JVM_IR
// FIR status: KT-57268 K2: extra methods `remove` and/or `getOrDefault` are generated for Map subclasses with JDK 1.6 in dependencies

class MyMap<K, V> : Map<K, V> {

    class MySet<E> : Set<E> {
        override fun contains(element: E): Boolean = TODO()
        override fun iterator(): Iterator<E> = TODO()
        override fun isEmpty(): Boolean = TODO()
        override fun containsAll(elements: Collection<E>): Boolean = TODO()
        override konst size: Int get() = TODO()
    }

    override konst entries get() = MySet<Map.Entry<K,V>>()
    override konst keys get() = MySet<K>()
    override konst size: Int get() = TODO()
    override konst konstues get() = ArrayList<V>()

    override fun containsKey(key: K): Boolean = TODO()
    override fun containsValue(konstue: V): Boolean = TODO()
    override fun get(key: K): V = TODO()
    override fun isEmpty(): Boolean = TODO()
}
