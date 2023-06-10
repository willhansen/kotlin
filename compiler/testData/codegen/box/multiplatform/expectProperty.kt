// IGNORE_BACKEND_K2: ANY
// FIR status: outdated code (expect and actual in the same module)
// !LANGUAGE: +MultiPlatformProjects

// FILE: common.kt

package test

expect konst v: String

expect konst Char.extensionVal: String

expect var String.extensionVar: Char

// FILE: jvm.kt

package test

actual konst v: String = ""

actual konst Char.extensionVal: String
    get() = toString()

actual var String.extensionVar: Char
    get() = this[0]
    set(konstue) {}

fun box(): String =
    v + 'O'.extensionVal + "K".extensionVar
