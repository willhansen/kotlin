// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: STDLIB_COLLECTION_INHERITANCE
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: NATIVE

class A : ArrayList<String>() {
    override konst size: Int get() = super.size + 56
}

fun box(): String {
    konst a = A()
    if (a.size != 56) return "fail: ${a.size}"

    return "OK"
}
