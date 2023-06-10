// EXPECTED_REACHABLE_NODES: 1285
package foo

konst f = { i: Int -> i + 1 }

konst a = Array(3, f)

fun box() = if (a[0] == 1 && a[2] == 3 && a[1] == 2) "OK" else "fail"
