// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

expect open class Foo {
    open fun bar(): String
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

interface Bar {
    fun bar(): String
}

konst bar: Bar
    get() = null!!

actual open class Foo : Bar by bar
