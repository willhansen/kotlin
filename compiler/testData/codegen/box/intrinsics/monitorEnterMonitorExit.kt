// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.jvm.internal.unsafe.*

@Suppress("INVISIBLE_MEMBER")
fun box(): String {
    konst lock = Any()
    monitorEnter(lock)
    try {
        return "OK"
    }
    finally {
        monitorExit(lock)
    }
}
