// IGNORE_BACKEND: WASM
// EXPECTED_REACHABLE_NODES: 1281
package foo

external konst c: Any? = definedExternally

fun box(): String {
    if (c != null) return "fail1"
    return if (c == null) "OK" else "fail2"
}
