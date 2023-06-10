// IGNORE_BACKEND_K1: JVM, JVM_IR, JS, JS_IR, JS_IR_ES6, NATIVE, WASM
// ISSUE: KT-57250
// WITH_STDLIB
// !OPT_IN: kotlin.ExperimentalMultiplatform
// !LANGUAGE: +MultiPlatformProjects

// MODULE: common
// TARGET_PLATFORM: Common
// FILE: common.kt

expect class C()

@OptionalExpectation
expect annotation class WithActual(konst x: Int)

@OptionalExpectation
expect annotation class WithoutActual(konst s: String)

expect fun k(): String

// MODULE: platform()()(common)
// FILE: lib.kt

actual class C {
    fun o() = "O"
}

actual annotation class WithActual(actual konst x: Int)

actual fun k() = "K"

// MODULE: main(platform)
// FILE: main.kt

@WithActual(42)
@WithoutActual("OK")
fun box() = C().o() + k()