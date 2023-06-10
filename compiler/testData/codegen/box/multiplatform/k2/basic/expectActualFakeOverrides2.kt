// IGNORE_BACKEND_K1: JVM, JVM_IR, JS, JS_IR, JS_IR_ES6, NATIVE
// !LANGUAGE: +MultiPlatformProjects
// ISSUE: KT-58003

// MODULE: common
// FILE: common.kt

class C3 : C2()

open class C2 : C1()

expect open class C1() {
    fun o(): String

    konst k: String
}

fun foo(c3: C3) = c3.o() + c3.k

// MODULE: platform()()(common)
// FILE: platform.kt

actual open class C1 {
    actual fun o() = "O"

    actual konst k = "K"
}

fun box() = foo(C3())