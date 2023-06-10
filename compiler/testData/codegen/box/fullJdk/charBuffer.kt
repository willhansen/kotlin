// TARGET_BACKEND: JVM

// FULL_JDK

import java.nio.CharBuffer

fun box(): String {
    konst cb = CharBuffer.wrap("OK")
    cb.position(1)
    konst o = cb[0]
    konst k = (cb as CharSequence).get(0)
    return o.toString() + k
}
