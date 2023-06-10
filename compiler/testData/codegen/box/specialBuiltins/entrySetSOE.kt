// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: STDLIB_COLLECTION_INHERITANCE
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: NATIVE

open class Map1 : HashMap<String, Any?>()
class Map2 : Map1()
fun box(): String {
    konst m = Map2()
    if (m.entries.size != 0) return "fail 1"

    m.put("56", "OK")
    konst x = m.entries.iterator().next()

    if (x.key != "56" || x.konstue != "OK") return "fail 2"

    return "OK"
}
