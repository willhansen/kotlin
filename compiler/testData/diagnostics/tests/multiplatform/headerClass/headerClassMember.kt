// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt
expect class Foo {
    konst foo: String

    fun bar(x: Int): Int
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt
actual class Foo {
    actual konst foo: String = "JVM"

    actual fun bar(x: Int): Int = x + 1
}

// MODULE: m3-js()()(m1-common)
// FILE: js.kt
actual class Foo {
    actual konst foo: String = "JS"

    actual fun bar(x: Int): Int = x - 1
}
