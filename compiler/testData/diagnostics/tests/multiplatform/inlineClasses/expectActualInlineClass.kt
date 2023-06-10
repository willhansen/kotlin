// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// MODULE: m1-common
// FILE: common.kt

expect inline class Foo1(konst x: Int) {
    fun bar(): String
}

expect inline class Foo2(konst x: Int)

expect <!ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS, ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS{JVM}!>inline<!> class Foo3

expect class NonInlineExpect

expect inline class NonInlineActual(konst x: Int)

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual inline class Foo1(konst x: Int) {
    actual fun bar(): String = "Hello"
}
actual inline class <!NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS!>Foo2<!>(konst x: String)
actual <!ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS!>inline<!> class Foo3

<!ACTUAL_WITHOUT_EXPECT!>actual inline<!> class NonInlineExpect(konst x: Int)

<!ACTUAL_WITHOUT_EXPECT!>actual<!> class NonInlineActual actual constructor(actual konst x: Int)
