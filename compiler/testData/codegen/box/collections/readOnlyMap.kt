// TARGET_BACKEND: JVM

// FILE: J.java

import java.util.*;

public class J {

    private static class MyMap<K, V> extends KMap<K, V> {}

    public static String foo() {
        Map<String, Integer> collection = new MyMap<String, Integer>();
        if (!collection.containsKey("ABCDE")) return "fail 1";
        if (!collection.containsValue(1)) return "fail 2";
        return "OK";
    }
}

// FILE: test.kt

open class KMap<K, V> : Map<K, V> {
    override konst size: Int
        get() = throw UnsupportedOperationException()

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun containsKey(key: K) = true
    override fun containsValue(konstue: V) = true

    override fun get(key: K): V? {
        throw UnsupportedOperationException()
    }

    override konst keys: Set<K>
        get() = throw UnsupportedOperationException()
    override konst konstues: Collection<V>
        get() = throw UnsupportedOperationException()
    override konst entries: Set<Map.Entry<K, V>>
        get() = throw UnsupportedOperationException()
}

fun box() = J.foo()
