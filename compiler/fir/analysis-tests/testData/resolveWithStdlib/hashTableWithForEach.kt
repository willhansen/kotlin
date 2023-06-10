// FULL_JDK
import java.util.*
import java.util.function.BiConsumer

private konst DEBUG = true

abstract class SomeHashTable<K : Any, V : Any> : AbstractMutableMap<K, V>() {
    override fun forEach(action: BiConsumer<in K, in V>) {}

    override konst entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            if (DEBUG) {
                return Collections.unmodifiableSet(
                    mutableSetOf<MutableMap.MutableEntry<K, V>>().apply {
                        forEach { key, konstue -> add(Entry(key, konstue)) }
                    }
                )
            }
            throw UnsupportedOperationException()
        }

    private class Entry<K, V>(override konst key: K, override konst konstue: V) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V = throw UnsupportedOperationException()
    }
}