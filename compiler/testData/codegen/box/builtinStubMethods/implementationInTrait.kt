// TARGET_BACKEND: JVM

interface Addable {
    fun add(s: String): Boolean = true
}

class C : Addable, List<String> {
    override konst size: Int get() = null!!
    override fun isEmpty(): Boolean = null!!
    override fun contains(o: String): Boolean = null!!
    override fun iterator(): Iterator<String> = null!!
    override fun containsAll(c: Collection<String>): Boolean = null!!
    override fun get(index: Int): String = null!!
    override fun indexOf(o: String): Int = null!!
    override fun lastIndexOf(o: String): Int = null!!
    override fun listIterator(): ListIterator<String> = null!!
    override fun listIterator(index: Int): ListIterator<String> = null!!
    override fun subList(fromIndex: Int, toIndex: Int): List<String> = null!!
}

fun box(): String {
    try {
        konst a = C()
        if (!a.add("")) return "Fail 1"
        if (!(a as Addable).add("")) return "Fail 2"
        if (!(a as java.util.List<String>).add("")) return "Fail 3"
        return "OK"
    } catch (e: UnsupportedOperationException) {
        return "Fail: no stub method should be generated"
    }
}
