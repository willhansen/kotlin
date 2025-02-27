// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: STDLIB_COLLECTION_INHERITANCE
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: NATIVE
class A : HashSet<Long>()

fun box(): String {
    konst a = A()
    konst b = A()

    a.iterator()

    a.add(0L)
    a.remove(0L)

    a.addAll(b)
    a.removeAll(b)
    a.retainAll(b)
    a.clear()

    return "OK"
}
