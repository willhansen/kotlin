// IGNORE_BACKEND_K1: JVM, JVM_IR, JS, JS_IR, JS_IR_ES6, NATIVE
// !LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt

class C2 : C1() {
    override fun o() = super.o()

    override konst k = "K"
}

expect open class C1() {
    open fun o(): String

    open konst k: String
}

fun foo(c2: C2) = c2.o() + c2.k

// MODULE: platform()()(common)
// FILE: platform.kt

actual open class C1 {
    actual open fun o() = "O"

    actual open konst k = "K"
}

fun box() = foo(C2())