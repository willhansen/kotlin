// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {
    konst a = "abc"
    konst b = "abc"
    konst c = "def"

    if (a != b) return "fail1"
    if (a == c) return "fail2"

    return "OK"
}