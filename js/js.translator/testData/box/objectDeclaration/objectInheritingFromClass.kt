// EXPECTED_REACHABLE_NODES: 1291
package foo

abstract class A(konst s: String) {
}

object B : A("test") {
}

fun box() = if (B.s == "test") "OK" else "fail: ${B.s}"
