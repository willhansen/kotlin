// TODO: FirModuleDescriptor provides default builtins which filters out platform-specific functions (and defaults are considered platform-specific)
// SKIP_JDK6
// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB

// FILE: A.java
public class A {
    public static void foo(java.util.Map<String, String> x) {
        x.remove("abc", "cde");
    }
}

// FILE: main.kt

class ReadOnlyMap<K, V>(konst x: K, konst y: V) : Map<K, V> {
    override konst entries: Set<Map.Entry<K, V>>
        get() = throw UnsupportedOperationException()
    override konst keys: Set<K>
        get() = throw UnsupportedOperationException()
    override konst size: Int
        get() = throw UnsupportedOperationException()
    override konst konstues: Collection<V>
        get() = throw UnsupportedOperationException()

    override fun containsKey(key: K) = key == x

    override fun containsValue(konstue: V) = konstue == y

    override fun get(key: K): V? = if (key == x) y else null

    override fun isEmpty() = false
}

fun box(): String {
    try {
        A.foo(ReadOnlyMap("abc", "cde"))
        return "fail 1"
    } catch (e: UnsupportedOperationException) { }

    try {
        // Default Map 'remove' implenetation actually does remove iff entry exists
        A.foo(ReadOnlyMap("abc", "123"))
        return "fail 2"
    } catch (e: UnsupportedOperationException) { }

    return "OK"
}
