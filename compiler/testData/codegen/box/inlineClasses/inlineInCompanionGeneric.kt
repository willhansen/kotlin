// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter
// FILE: R.kt
import kotlin.jvm.JvmInline

@JvmInline
konstue class R<T: String>(konst konstue: T) {
    companion object {
        inline fun ok() = R("OK")
    }
}

// FILE: test.kt

fun box(): String = R.ok().konstue


