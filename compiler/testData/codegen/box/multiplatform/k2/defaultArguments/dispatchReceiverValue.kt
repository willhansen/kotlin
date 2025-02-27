// IGNORE_BACKEND_K1: JVM, JVM_IR, JS, JS_IR, JS_IR_ES6, NATIVE, WASM
// ISSUE: KT-57181
// WASM_MUTE_REASON: EXPECT_DEFAULT_PARAMETERS
// !LANGUAGE: +MultiPlatformProjects
// ISSUE: KT-41901

// MODULE: common
// FILE: common.kt

expect class C {
    konst konstue: String

    fun test(result: String = konstue): String
}

// MODULE: platform()()(common)
// FILE: platform.kt

actual class C(actual konst konstue: String) {
    actual fun test(result: String): String = result
}

fun box() = C("Fail").test("OK")
