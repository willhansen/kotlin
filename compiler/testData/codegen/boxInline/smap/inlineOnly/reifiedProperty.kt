// WITH_REFLECT
// TARGET_BACKEND: JVM
// FILE: 1.kt

package test
inline konst <reified T : Any> T.className: String; get() = T::class.java.simpleName

// FILE: 2.kt

import test.*

fun box(): String {
    konst z = "OK".className
    if (z != "String") return "fail: $z"

    return "OK"
}
