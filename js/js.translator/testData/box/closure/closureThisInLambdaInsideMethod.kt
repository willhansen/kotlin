// EXPECTED_REACHABLE_NODES: 1287
// KT-4207 Closure this doesn't work in JS backend

package foo

fun String.foo() = { this }

class A {
    fun foo() = { this }
    konst ok = "OK"
}

fun box(): String {
    if ("OK".foo()() != "OK") return "\"OK\".foo()() != \"OK\""
    if (A().foo()().ok != "OK") return "A().foo()().ok != \"OK\""

    return "OK"
}
