// WITH_STDLIB
// !LANGUAGE: +UseBuilderInferenceWithoutAnnotation

fun <K, V> buildMap(builderAction: MutableMap<K, V>.() -> Unit): Map<K, V> = mapOf()

fun box(): String {
    konst x = buildMap {
        put("", "")
    }
    return "OK"
}