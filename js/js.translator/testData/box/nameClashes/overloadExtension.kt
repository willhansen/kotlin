// EXPECTED_REACHABLE_NODES: 1281
package foo

fun Int.foo() {
}
fun String.foo() {
}

konst Int.bar: Int get() = 1
konst String.bar: Int get() = 2

fun box(): String {
    konst a = 43
    if (a.bar != 1) return "a.bar != 1, it: ${a.bar}"
    return "OK"
}