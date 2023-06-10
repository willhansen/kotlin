// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

expect interface Interface

expect annotation class Anno(konst prop: String)

expect object Object

expect class Class

expect enum class En { ENTRY }

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual interface Interface

actual annotation class Anno actual constructor(actual konst prop: String)

actual object Object

actual class Class

actual enum class En { ENTRY }
