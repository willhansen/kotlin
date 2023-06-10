// LANGUAGE: +MultiPlatformProjects, +ValueClasses
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB

// MODULE: common
// TARGET_PLATFORM: Common
// FILE: expect.kt

expect konstue class ExpectValue(konst x: String, konst y: String) {
    constructor(x: Int, y: Int)
}

// MODULE: main()()(common)
// TARGET_PLATFORM: JVM
// FILE: actual.kt

@JvmInline
actual konstue class ExpectValue actual constructor(actual konst x: String, actual konst y: String) {
    actual constructor(x: Int, y: Int) : this(if (x == 42) "O" else "Not OK: $x\n", if (y == -42) "K" else "Not OK: $y")
}

fun box() = ExpectValue(42, -42).run { x + y }
