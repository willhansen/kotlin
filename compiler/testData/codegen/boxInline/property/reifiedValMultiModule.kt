// WITH_STDLIB
// WITH_REFLECT
// MODULE: lib
// FILE: lib.kt
package lib

inline konst <reified T: Any> T.konstue: String
    get() = T::class.simpleName!!

// MODULE: main(lib)
// FILE: box.kt
import lib.*

class OK

fun box(): String {
    return OK().konstue ?: "fail"
}
