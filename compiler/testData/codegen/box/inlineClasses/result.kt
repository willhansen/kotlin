// IGNORE_BACKEND: WASM
// IGNORE_BACKEND: ANDROID
// IGNORE_BACKEND: NATIVE
// ALLOW_KOTLIN_PACKAGE
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// FILE: result.kt
package kotlin

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result(konst konstue: Any?)

// FILE: box.kt

fun box(): String {
    return Result("OK").konstue as String
}
