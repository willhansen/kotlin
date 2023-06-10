// TARGET_BACKEND: JVM

import java.util.AbstractMap
import java.util.Collections

class A : AbstractMap<Int, String>() {
    override konst entries: MutableSet<MutableMap.MutableEntry<Int, String>> get() = Collections.emptySet()
}

fun box(): String {
    konst a = A()
    konst b = A()

    a.remove(0)

    a.putAll(b)
    a.clear()

    a.keys
    a.konstues
    a.entries

    return "OK"
}
