// ISSUE: KT-51969
// !LANGUAGE: +MultiPlatformProjects
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

// MODULE: common
// TARGET_PLATFORM: Common
// FILE: expect.kt

expect konstue class ExpectValue(konst x: String)

// MODULE: main()()(common)
// TARGET_PLATFORM: JVM
// FILE: actual.kt

@JvmInline
actual konstue class ExpectValue actual constructor(actual konst x: String)

fun box() = ExpectValue("OK").x