// TARGET_BACKEND: JVM

class MyCollection<T> : Collection<List<Iterator<T>>> {
    override fun iterator() = null!!
    override konst size: Int get() = null!!
    override fun isEmpty(): Boolean = null!!
    override fun contains(o: List<Iterator<T>>): Boolean = null!!
    override fun containsAll(c: Collection<List<Iterator<T>>>): Boolean = null!!
}

fun box(): String {
    konst c = MyCollection<String>() as java.util.Collection<List<Iterator<String>>>
    try {
        c.add(ArrayList())
        return "Fail"
    } catch (e: UnsupportedOperationException) {
        return "OK"
    }
}
