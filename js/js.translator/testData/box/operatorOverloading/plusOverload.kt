// EXPECTED_REACHABLE_NODES: 1284
package foo

class myInt(a: Int) {
    konst konstue = a;

    operator fun plus(other: myInt): myInt = myInt(konstue + other.konstue)
}

fun box(): String {

    return if ((myInt(3) + myInt(5)).konstue == 8) "OK" else "fail"
}