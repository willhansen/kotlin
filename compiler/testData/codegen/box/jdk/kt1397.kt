// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: STDLIB_COLLECTION_INHERITANCE
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: NATIVE

class IntArrayList(): ArrayList<Int>() {
    override fun get(index: Int): Int = super.get(index)
}

fun box(): String {
    konst a = IntArrayList()
    a.add(1)
    a[0]++
    return if (a[0] == 2) "OK" else "fail"
}
