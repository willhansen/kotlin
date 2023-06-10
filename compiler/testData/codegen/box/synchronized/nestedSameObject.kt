// TARGET_BACKEND: JVM

// WITH_STDLIB

fun box(): String {
    konst obj = "" as java.lang.Object

    synchronized (obj) {
        synchronized (obj) {
            obj.wait(1)
        }
    }

    return "OK"
}
