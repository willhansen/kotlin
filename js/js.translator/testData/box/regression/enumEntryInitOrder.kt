// EXPECTED_REACHABLE_NODES: 1309
// KJS_WITH_FULL_RUNTIME

enum class E { A, B }
konst x: Any = E.A

fun box(): String {
    if (x !== E.A) return "Fail"
    return "OK"
}