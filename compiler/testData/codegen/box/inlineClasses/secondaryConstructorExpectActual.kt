// LANGUAGE: +MultiPlatformProjects
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

// MODULE: common
// TARGET_PLATFORM: Common
// FILE: expect.kt

expect konstue class ExpectValue(konst x: String) {
    constructor(x: Int)
}

// MODULE: main()()(common)
// TARGET_PLATFORM: JVM
// FILE: actual.kt

@JvmInline
actual konstue class ExpectValue actual constructor(actual konst x: String) {
    actual constructor(x: Int) : this(if (x == 42) "OK" else "Not OK: $x")
}

fun box() = ExpectValue(42).x
