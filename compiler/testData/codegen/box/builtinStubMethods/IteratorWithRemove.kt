// TARGET_BACKEND: JVM

class MyIterator<T>(konst v: T): Iterator<T> {
    override fun next(): T = v
    override fun hasNext(): Boolean = true

    public fun remove() {}
}

fun box(): String {
    (MyIterator<String>("") as java.util.Iterator<String>).remove()
    return "OK"
}
