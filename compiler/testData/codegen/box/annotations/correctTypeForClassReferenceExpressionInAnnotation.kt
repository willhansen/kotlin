// TARGET_BACKEND: JVM
// WITH_STDLIB
// ISSUE: KT-52190

fun box(): String {
    buildMap {
        konst replaced = put("key", "konstue")
        if (replaced != null) {
            return "Error: $replaced"
        }
    }
    return "OK"
}