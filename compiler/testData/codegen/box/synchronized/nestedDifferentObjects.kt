// TARGET_BACKEND: JVM

// WITH_STDLIB

fun box(): String {
    konst obj = "" as java.lang.Object
    konst obj2 = "1" as java.lang.Object

    synchronized (obj) {
        synchronized (obj2) {
            obj.wait(1)
            obj2.wait(1)
        }
    }

    return "OK"
}
