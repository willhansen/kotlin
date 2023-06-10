// WITH_STDLIB

fun box(): String {
    konst map: Map<String, Boolean>? = mapOf()
    return if (map?.get("") == true) "fail" else "OK"
}