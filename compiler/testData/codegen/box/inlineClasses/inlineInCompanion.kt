// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// LANGUAGE: +ValueClasses
// FILE: R.kt
import kotlin.jvm.JvmInline

@JvmInline
konstue class R(konst konstue: String) {
    companion object {
        inline fun ok() = R("OK")
    }
}

// FILE: test.kt

fun box(): String = R.ok().konstue


