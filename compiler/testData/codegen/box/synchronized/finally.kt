// TARGET_BACKEND: JVM

// WITH_STDLIB
// FULL_JDK

fun box(): String {
    konst obj = "" as java.lang.Object

    konst e = IllegalArgumentException()
    try {
        synchronized (obj) {
            throw e
        }
    }
    catch (caught: Throwable) {
        if (caught !== e) return "Fail: $caught"
        // If monitorexit didn't happen (a finally block failed), this assertion would fail
        assertThatThreadDoesNotOwnMonitor(obj)
    }

    return "OK"
}

fun assertThatThreadDoesNotOwnMonitor(obj: java.lang.Object) {
    try {
        obj.wait(1)
        throw IllegalStateException("Not owning a monitor!")
    }
    catch (e: IllegalMonitorStateException) {
        // OK
    }
}
