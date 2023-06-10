// IGNORE_BACKEND_K2: ANY
// !LANGUAGE: +MultiPlatformProjects
// FIR status: outdated code (expect and actual in the same module)

// FILE: common.kt

class Receiver(konst konstue: String)

expect fun Receiver.test(result: String = konstue): String

// FILE: platform.kt

actual fun Receiver.test(result: String): String {
    return result
}

fun box() = Receiver("Fail").test("OK")
