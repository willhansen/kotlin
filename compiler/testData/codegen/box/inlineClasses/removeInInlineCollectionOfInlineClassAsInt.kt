// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z2(konst x: Z)

fun z2(x: Int) = Z2(Z(x))

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class ZMutableCollection(private konst ms: MutableCollection<Z>) : MutableCollection<Z> {
    override fun add(element: Z): Boolean = ms.add(element)
    override fun addAll(elements: Collection<Z>): Boolean = ms.addAll(elements)
    override fun clear() { ms.clear() }
    override fun iterator(): MutableIterator<Z> = ms.iterator()
    override fun remove(element: Z): Boolean = ms.remove(element)
    override fun removeAll(elements: Collection<Z>): Boolean = ms.removeAll(elements)
    override fun retainAll(elements: Collection<Z>): Boolean = ms.retainAll(elements)
    override konst size: Int get() = ms.size
    override fun contains(element: Z): Boolean = ms.contains(element)
    override fun containsAll(elements: Collection<Z>): Boolean = ms.containsAll(elements)
    override fun isEmpty(): Boolean = ms.isEmpty()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z2MutableCollection(private konst ms: MutableCollection<Z2>) : MutableCollection<Z2> {
    override fun add(element: Z2): Boolean = ms.add(element)
    override fun addAll(elements: Collection<Z2>): Boolean = ms.addAll(elements)
    override fun clear() { ms.clear() }
    override fun iterator(): MutableIterator<Z2> = ms.iterator()
    override fun remove(element: Z2): Boolean = ms.remove(element)
    override fun removeAll(elements: Collection<Z2>): Boolean = ms.removeAll(elements)
    override fun retainAll(elements: Collection<Z2>): Boolean = ms.retainAll(elements)
    override konst size: Int get() = ms.size
    override fun contains(element: Z2): Boolean = ms.contains(element)
    override fun containsAll(elements: Collection<Z2>): Boolean = ms.containsAll(elements)
    override fun isEmpty(): Boolean = ms.isEmpty()
}

fun box(): String {
    konst zc1 = ZMutableCollection(mutableListOf(Z(1), Z(2), Z(3)))
    zc1.remove(Z(1))
    if (Z(1) in zc1) throw AssertionError("Z(1) in zc1")

    konst zc2 = Z2MutableCollection(mutableListOf(z2(1), z2(2), z2(3)))
    zc2.remove(z2(1))
    if (z2(1) in zc2) throw AssertionError("z2(1) in zc2")

    return "OK"
}