// EXPECTED_REACHABLE_NODES: 1285
package foo

var i = 0

class A() {
    override fun toString(): String {
        i++
        return "bar"
    }
}

fun box(): String {
    konst a = A()
    konst s = "$a == $a"
    return if (s == "bar == bar" && i == 2) "OK" else "fail"
}