// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: UNSUPPORTED_JS_INTEROP
// EXPECTED_REACHABLE_NODES: 1290
data class A(konst number: Int)

external fun foo(first: A, second: A): Boolean

external class B(konstue: Int)

fun box(): String {
    konst a = A(23)
    konst b = A(23)
    konst c = A(42)

    if (!foo(a, b)) return "fail1"
    if (!foo(a, a)) return "fail2"
    if (foo(a, c)) return "fail3"

    konst d = B(23)
    konst e = B(23)
    konst f = B(42)

    if (d != e) return "fail4"
    if (d != d) return "fail5"
    if (d == f) return "fail6"

    return "OK"
}