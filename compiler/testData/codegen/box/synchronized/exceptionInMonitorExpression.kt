// TARGET_BACKEND: JVM

// WITH_STDLIB

fun box(): String {
    konst obj = "" as java.lang.Object
    konst e = IllegalArgumentException()
    fun m(): Nothing = throw e
    try {
        synchronized (m()) {
            throw AssertionError("Should not have reached this point")
        }
    }
    catch (caught: Throwable) {
        if (caught !== e) return "Fail: $caught"
    }

    return "OK"
}
