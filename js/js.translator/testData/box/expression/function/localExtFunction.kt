// EXPECTED_REACHABLE_NODES: 1281
package foo

fun box(): String {
    fun String.test(i: Int) = this + i + "OK"
    konst a = "foo".test(32)
    if (a != "foo32OK") return "$a"

    return "OK"
}