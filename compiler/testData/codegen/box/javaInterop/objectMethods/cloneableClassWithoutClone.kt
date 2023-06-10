// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE

data class A(konst s: String) : Cloneable {
    fun externalClone(): A = clone() as A
}

fun box(): String {
    konst a = A("OK")
    konst b = a.externalClone()
    if (a != b) return "Fail equals"
    if (a === b) return "Fail identity"
    return b.s
}
