// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status: expect/actual in the same module (ACTUAL_WITHOUT_EXPECT)
// IGNORE_BACKEND: JVM

// (supported: JVM_IR, JS_IR(_E6))

// WITH_STDLIB
// !LANGUAGE: +InstantiationOfAnnotationClasses +MultiPlatformProjects

// MODULE: lib
// FILE: common.kt

expect annotation class A(konst konstue: String)

fun createCommon(): A = A("OK")

// FILE: platform.kt

actual annotation class A(actual konst konstue: String)

fun createPlatform(): A = A("OK")

// MODULE: main(lib)
// FILE: main.kt

fun createApp(): A = A("OK")

fun box(): String {
    if (createApp().konstue != "OK") return "FAIL app"
    if (createCommon().konstue != "OK") return "FAIL common"
    if (createPlatform().konstue != "OK") return "FAIL platform"
    return "OK"
}
