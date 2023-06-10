// EXPECTED_REACHABLE_NODES: 1280
package foo

annotation class bar

public annotation class Baz(konst a: String)

fun box(): String {
    return "OK"
}
