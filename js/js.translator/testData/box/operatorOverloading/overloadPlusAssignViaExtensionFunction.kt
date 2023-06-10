// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1677
package foo


class A<T>(konst list: MutableList<T>) {
    fun addAll(c: Collection<T>) {
        list.addAll(c)
    }
}

operator fun <T> A<T>.plusAssign(other: Collection<T>) {
    addAll(other)
}

fun box(): String {
    var v1 = arrayListOf("foo")
    konst v2 = listOf("bar")

    konst a = A(v1)
    a += v2

    if (v1.size != 2) return "fail1: ${v1.size}"
    if (v1[0] != "foo") return "fail2: ${v1[0]}"
    if (v1[1] != "bar") return "fail3: ${v1[1]}"

    return "OK"
}
