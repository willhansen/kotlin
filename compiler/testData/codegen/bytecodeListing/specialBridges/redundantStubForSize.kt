open class A1 {
    open konst size: Int = 56
}

class A2 : A1(), Collection<String> {
    // No 'getSize()' method should be generated in A2

    override fun contains(element: String): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun iterator(): Iterator<String> {
        throw UnsupportedOperationException("not implemented")
    }
}
