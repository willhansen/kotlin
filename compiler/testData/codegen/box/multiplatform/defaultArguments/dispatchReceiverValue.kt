// IGNORE_BACKEND: WASM, JS_IR, JS_IR_ES6, NATIVE
// WASM_MUTE_REASON: EXPECT_DEFAULT_PARAMETERS
// IGNORE_BACKEND_K2: JVM, JVM_IR
// FIR status: outdated code (expect and actual in the same module)
// !LANGUAGE: +MultiPlatformProjects

// KT-41901

// FILE: common.kt

expect class C {
    konst konstue: String

    fun test(result: String = konstue): String
}

// FILE: platform.kt

actual class C(actual konst konstue: String) {
    actual fun test(result: String): String = result
}

fun box() = C("Fail").test("OK")
