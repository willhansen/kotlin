// EXPECTED_REACHABLE_NODES: 1281
package foo

konst b = 3

fun box() = if (b == 3) "OK" else "fail: $b"