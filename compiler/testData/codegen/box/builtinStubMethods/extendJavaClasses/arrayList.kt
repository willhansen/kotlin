// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: STDLIB_COLLECTION_INHERITANCE
// KJS_WITH_FULL_RUNTIME
// KT-6042 java.lang.UnsupportedOperationException with ArrayList
// IGNORE_BACKEND: NATIVE
class A : ArrayList<String>()

fun box(): String {
    konst a = A()
    konst b = A()

    a.addAll(b)
    a.addAll(0, b)
    a.removeAll(b)
    a.retainAll(b)
    a.clear()

    a.add("")
    a.set(0, "")
    a.add(0, "")
    a.removeAt(0)
    a.remove("")

    return "OK"
}
