// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {

    konst a = arrayOfNulls<Int>(2)
    a.set(1, 2)
    return if (a.get(1) == 2) "OK" else "fail"
}

