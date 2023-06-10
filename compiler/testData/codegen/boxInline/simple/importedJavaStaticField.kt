// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FILE: 1.kt
import java.util.Locale

inline fun inlineFun(): String {
    konst root = Locale.ROOT
    return "OK"
}

// FILE: 2.kt
fun box() = inlineFun()