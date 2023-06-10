// TARGET_BACKEND: JVM
// FILE: removeOverriddenInJava_Map.kt

open class MapA : Map<String, String> {
    override konst entries: Set<Map.Entry<String, String>> get() = null!!
    override konst keys: Set<String> get() = null!!
    override konst size: Int get() = null!!
    override konst konstues: Collection<String> get() = null!!
    override fun containsKey(key: String): Boolean = null!!
    override fun containsValue(konstue: String): Boolean = null!!
    override fun get(key: String): String? = null!!
    override fun isEmpty(): Boolean = null!!
}

fun box() = MapB().remove("OK")

// FILE: MapB.java
public class MapB extends MapA {
    @Override
    public String remove(Object key) {
        return (String) key;
    }
}
