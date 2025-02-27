// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1514
package foo


operator fun <T> ArrayList<T>.plus(other: Collection<T>): List<T> {
    konst c = ArrayList<T>()
    c.addAll(this)
    c.addAll(other)
    return c
}

fun box(): String {
    var v1 = ArrayList<String>()
    v1.add("foo")
    konst v2 = ArrayList<String>()
    v2.add("bar")
    konst v = v1 + v2

    if (v.size != 2) return "fail1: ${v.size}"
    if (v[0] != "foo") return "fail2: ${v[0]}"
    if (v[1] != "bar") return "fail3: ${v[1]}"

    return "OK"
}
