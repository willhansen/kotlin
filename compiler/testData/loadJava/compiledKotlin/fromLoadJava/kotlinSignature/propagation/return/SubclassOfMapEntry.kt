package test

public interface SubclassOfMapEntry<K, V>: MutableMap.MutableEntry<K, V> {
    override konst konstue: V
    override fun setValue(konstue: V) : V
}
