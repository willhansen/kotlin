// TARGET_BACKEND: JVM

class MyMapEntry<K, V>: Map.Entry<K, V> {
    override fun hashCode(): Int = 0
    override fun equals(other: Any?): Boolean = false
    override konst key: K get() = throw UnsupportedOperationException()
    override konst konstue: V get() = throw UnsupportedOperationException()
}

fun box(): String {
    try {
        (MyMapEntry<String, Int>() as java.util.Map.Entry<String, Int>).setValue(1)
        throw AssertionError()
    } catch (e: UnsupportedOperationException) {
        return "OK"
    }
}
