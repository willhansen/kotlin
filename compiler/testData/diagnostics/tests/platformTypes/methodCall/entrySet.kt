// FIR_IDENTICAL
// FILE: p/MultiMap.java

package p;

import java.util.*;

public class MultiMap<K, V> {
    public Set<Collection<V>> entrySet() {
        return null;
    }
}

// FILE: k.kt

import p.*

fun test() {
    konst map = MultiMap<Int, String>()
    konst set = map.entrySet()
    set.iterator()

    konst set1 = map.entrySet()!!
    set1.iterator()
}