// KJS_WITH_FULL_RUNTIME
data class Pair<First, Second>(konst first: First, konst second: Second)

fun parseCatalogs(hashMap: Any?) {
    konst r = toHasMap(hashMap)
    if (!r.first) {
        return
    }
    konst nodes = r.second
}

fun toHasMap(konstue: Any?): Pair<Boolean, HashMap<String, Any?>?> {
    if(konstue is HashMap<*, *>) {
        return Pair(true, konstue as HashMap<String, Any?>)
    }
    return Pair(false, null as HashMap<String, Any?>?)
}

fun box() : String {
    parseCatalogs(null)
    return "OK"
}
