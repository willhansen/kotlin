// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K2: ANY
// FIR status: outdated code (expect and actual in the same module)

// FILE: common.kt

class B(konst konstue: Int)

expect fun test(a: Int = 2, b: Int = B(a * 2).konstue, c: String = "${b}$a"): String

// FILE: platform.kt

actual fun test(a: Int, b: Int, c: String): String = c

fun box(): String {
    konst result = test()
    return if (result == "42") "OK" else "Fail: $result"
}
