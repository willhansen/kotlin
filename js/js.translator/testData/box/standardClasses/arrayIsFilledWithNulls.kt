// EXPECTED_REACHABLE_NODES: 1366
package foo

konst a = arrayOfNulls<Int>(3)

fun box() = if (a[0] == null && a[1] == null && a[2] == null) "OK" else "fail"