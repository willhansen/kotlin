// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

expect class Foo {
    konst bar: String
}

// MODULE: m1-jvm()()(m1-common)
// FILE: jvm.kt

actual class Foo {
    actual konst bar = "bar"
    fun bar() = bar
}
