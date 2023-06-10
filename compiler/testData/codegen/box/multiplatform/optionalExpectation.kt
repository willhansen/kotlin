// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status: expect/actual in the same module (ACTUAL_WITHOUT_EXPECT)
// !LANGUAGE: +MultiPlatformProjects
// !OPT_IN: kotlin.ExperimentalMultiplatform
// WITH_STDLIB
// MODULE: library
// FILE: expected.kt

package a

@OptionalExpectation
expect annotation class A(konst x: Int)

@OptionalExpectation
expect annotation class B(konst s: String) {
    @OptionalExpectation
    annotation class C(konst a: Boolean)
}

// FILE: actual.kt

package a

actual annotation class A(actual konst x: Int)

// MODULE: main(library)
// FILE: main.kt

@file:Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // TODO: support common sources in the test infrastructure

package usage

import a.A
import a.B

@A(42)
@B("OK")
@B.C(true)
fun box(): String {
    return "OK"
}
