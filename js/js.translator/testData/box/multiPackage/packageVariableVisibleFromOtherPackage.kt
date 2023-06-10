// EXPECTED_REACHABLE_NODES: 1282
// FILE: a.kt
package foo

konst a = 2

fun box() = if ((a + bar.a) == 5) "OK" else "fail"


// FILE: b.kt
package bar

konst a = 3
