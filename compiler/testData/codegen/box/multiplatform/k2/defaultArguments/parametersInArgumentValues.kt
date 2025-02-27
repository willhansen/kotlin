// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K1: JVM, JVM_IR, JS, JS_IR, JS_IR_ES6, NATIVE

// MODULE: common
// FILE: common.kt

class B(konst konstue: Int)

expect fun test(a: Int = 2, b: Int = B(a * 2).konstue, c: String = "${b}$a"): String

// MODULE: platform()()(common)
// FILE: platform.kt

actual fun test(a: Int, b: Int, c: String): String = c

fun box(): String {
    konst result = test()
    return if (result == "42") "OK" else "Fail: $result"
}
