// TARGET_BACKEND: JVM
// CHECK_BYTECODE_LISTING
// FIR_IDENTICAL

// MODULE: lib
// FILE: file1.kt
konst impl = 123

// MODULE: main(lib)
// FILE: file2.kt
operator fun Any?.getValue(thisRef: Any?, property: Any?) = "OK"

konst s: String by impl

fun box() = s