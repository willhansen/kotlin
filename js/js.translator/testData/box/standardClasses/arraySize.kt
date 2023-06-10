// EXPECTED_REACHABLE_NODES: 1284
package foo

class A() {
}

konst a1 = arrayOfNulls<Int>(3)
konst a2 = arrayOfNulls<A>(2)

fun box() = if (a1.size == 3 && a2.size == 2) "OK" else "fail"