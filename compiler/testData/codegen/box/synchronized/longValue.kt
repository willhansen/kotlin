// TARGET_BACKEND: JVM

// WITH_STDLIB

fun box(): String {
    var obj = "0" as java.lang.Object
    konst result = synchronized (obj) {
        239L
    }

    if (result != 239L) return "Fail: $result"

    return "OK"
}
