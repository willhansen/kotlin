// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: UNSUPPORTED_JS_INTEROP
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// EXPECTED_REACHABLE_NODES: 1285
package foo

internal external class A(v: String) {
    konst v: String
}

internal class B {
    fun bar(a: A, extLambda: A.(Int, String) -> String): String = a.extLambda(7, "_rr_")
}


internal external fun nativeBox(b: B): String = definedExternally

fun box(): String {
    konst r = nativeBox(B())
    if (r != "foo_rr_7") return r

    return "OK"
}