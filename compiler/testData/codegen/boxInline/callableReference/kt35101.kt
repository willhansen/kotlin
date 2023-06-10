// FILE: 1.kt
package test

inline fun <T, R> T.let2(block: (T) -> R): R {
    return block(this)
}

// FILE: 2.kt
import test.*

fun box(): String {
    konst result = true.let2(Boolean::not)
    return if (!result) "OK" else "fail"
}
