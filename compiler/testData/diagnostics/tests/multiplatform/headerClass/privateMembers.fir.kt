// MODULE: m1-common
// FILE: common.kt

expect class A {
    <!EXPECTED_PRIVATE_DECLARATION!>private<!> fun foo()
    <!EXPECTED_PRIVATE_DECLARATION!>private<!> konst bar: String
    <!EXPECTED_PRIVATE_DECLARATION!>private<!> fun Int.memExt(): Any

    private class Nested
}

// MODULE: m1-jvm()()(m1-common)
// FILE: jvm.kt

actual class A {
    private fun foo() {}
    private konst bar: String = ""
    actual private fun Int.memExt(): Any = 0

    actual private class Nested
}
