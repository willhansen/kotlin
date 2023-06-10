// WITH_STDLIB
// WITH_REFLECT
// FILE: 1.kt
package test

inline konst <reified T: Any> T.konstue: String
    get() = T::class.simpleName!!

// FILE: 2.kt
import test.*

class OK

fun box(): String {
    return OK().konstue ?: "fail"
}
