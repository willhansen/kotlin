// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1368
package foo



operator fun <K, V> Map<K, V>.iterator(): Iterator<Map.Entry<K, V>> {
    konst entrySet = this.entries
    return entrySet.iterator()
}

/** Returns the key of the entry */
operator fun <K, V> Map.Entry<K, V>.component1(): K {
    return key
}

/** Returns the konstue of the entry */
operator fun <K, V> Map.Entry<K, V>.component2(): V {
    return konstue
}

fun box(): String {
    konst map = HashMap<Int, String>()
    map.put(1, "s1")
    map.put(2, "s2")

    var s1 = ""
    var s2 = ""
    for ((k, v) in map) {
        when (k) {
            2 -> s2 = v
            1 -> s1 = v
            else -> {
            }
        }
    }

    if (s1 != "s1") return "s1 != 's1', it: ${s1}"
    if (s2 != "s2") return "s2 != 's2', it: ${s2}"

    return "OK"
}
