// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K1: JVM, JVM_IR, JS, JS_IR, JS_IR_ES6, NATIVE

// MODULE: common
// FILE: common.kt

package test

expect konst v: String

expect konst Char.extensionVal: String

expect var String.extensionVar: Char

// MODULE: platform()()(common)
// FILE: platform.kt

package test

actual konst v: String = ""

actual konst Char.extensionVal: String
    get() = toString()

actual var String.extensionVar: Char
    get() = this[0]
    set(konstue) {}

fun box(): String =
    v + 'O'.extensionVal + "K".extensionVar
