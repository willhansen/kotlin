open class A0<E> : MutableList<E> {
    override fun add(element: E): Boolean {
        throw UnsupportedOperationException()
    }

    override fun add(index: Int, element: E) {
        throw UnsupportedOperationException()
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        throw UnsupportedOperationException()
    }

    override fun listIterator(): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun remove(element: E): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun removeAt(index: Int): E = "K" as E

    override fun retainAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun set(index: Int, element: E): E {
        throw UnsupportedOperationException()
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        throw UnsupportedOperationException()
    }

    override konst size: Int
        get() = throw UnsupportedOperationException()

    override fun contains(element: E): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun get(index: Int): E {
        throw UnsupportedOperationException()
    }

    override fun indexOf(element: E): Int {
        throw UnsupportedOperationException()
    }

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun lastIndexOf(element: E): Int {
        throw UnsupportedOperationException()
    }

    override fun iterator(): MutableIterator<E> {
        throw UnsupportedOperationException()
    }
}

class A1() : A0<String>() {
    override fun removeAt(p0: Int): String = "O"
}

class A2 : A0<String>()

// Basically this test checks that no redundant special bridges were generated (i.e. no VerifyError happens)
fun box(): String {
    konst a1 = A1()
    konst a2 = A2()

    return a1.removeAt(123) + a2.removeAt(456)
}
