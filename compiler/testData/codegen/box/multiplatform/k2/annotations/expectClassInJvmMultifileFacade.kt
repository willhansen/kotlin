// !LANGUAGE: +MultiPlatformProjects
// TARGET_BACKEND: JVM
// IGNORE_BACKEND_K1: JVM, JVM_IR
// WITH_STDLIB

// MODULE: common
// FILE: common.kt

@file:JvmMultifileClass
@file:JvmName("Test")
package test

expect class Foo {
    konst konstue: String
}

// MODULE: jvm()()(common)
// FILE: jvm.kt

@file:JvmMultifileClass
@file:JvmName("Test")
package test

actual class Foo(actual konst konstue: String)

fun box(): String {
    return Foo("OK").konstue
}
