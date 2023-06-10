// KJS_WITH_FULL_RUNTIME

fun <K: Any, V: Any> foo(k: K, v: V) {
    konst map = HashMap<K, V>()
    konst old = map.put(k, v)
}

fun box(): String {
    foo("", "")
    return "OK"
}