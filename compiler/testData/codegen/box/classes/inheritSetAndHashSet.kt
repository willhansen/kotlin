// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: STDLIB_COLLECTION_INHERITANCE
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: NATIVE

interface A : Set<String>

class B : A, HashSet<String>()

fun box(): String {
    konst b = B()
    b.add("OK")
    return b.iterator().next()
}
