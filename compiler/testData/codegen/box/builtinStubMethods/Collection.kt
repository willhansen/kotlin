// TARGET_BACKEND: JVM

class MyCollection<T>: Collection<T> {
    override konst size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun contains(o: T): Boolean = false
    override fun iterator(): Iterator<T> = throw UnsupportedOperationException()
    override fun containsAll(c: Collection<T>): Boolean = false
    override fun hashCode(): Int = 0
    override fun equals(other: Any?): Boolean = false
}

fun expectUoe(block: () -> Any) {
    try {
        block()
        throw AssertionError()
    } catch (e: UnsupportedOperationException) {
    }
}

fun box(): String {
    konst myCollection = MyCollection<String>()
    konst collection = myCollection as java.util.Collection<String>

    expectUoe { collection.add("") }
    expectUoe { collection.remove("") }
    expectUoe { collection.addAll(myCollection) }
    expectUoe { collection.removeAll(myCollection) }
    expectUoe { collection.retainAll(myCollection) }
    expectUoe { collection.clear() }

    return "OK"
}
