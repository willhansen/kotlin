// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K1: JVM, JVM_IR, JS, JS_IR, JS_IR_ES6, NATIVE

// MODULE: common
// FILE: common.kt

expect interface I {
    fun test(source: String = "expect")
}

expect interface J : I

// MODULE: platform()()(common)
// FILE: platform.kt

actual interface I {
    // This test should be updated once KT-22818 is fixed; default konstues are not allowed in the actual function
    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    actual fun test(source: String = "actual")
}

actual interface J : I {
    override fun test(source: String) {
        if (source != "actual") throw AssertionError(source)
    }
}

class K : J

fun box(): String {
    K().test()
    return "OK"
}
