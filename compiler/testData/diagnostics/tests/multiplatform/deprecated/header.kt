// MODULE: m1-common
// FILE: common.kt

<!DEPRECATED_MODIFIER, DEPRECATED_MODIFIER{JVM}!>header<!> class My

<!DEPRECATED_MODIFIER, DEPRECATED_MODIFIER{JVM}!>header<!> fun foo(): Int

<!DEPRECATED_MODIFIER, DEPRECATED_MODIFIER{JVM}!>header<!> konst x: String

<!DEPRECATED_MODIFIER, DEPRECATED_MODIFIER{JVM}!>header<!> object O

<!DEPRECATED_MODIFIER, DEPRECATED_MODIFIER{JVM}!>header<!> enum class E {
    FIRST
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

<!DEPRECATED_MODIFIER!>impl<!> class My

<!DEPRECATED_MODIFIER!>impl<!> fun foo() = 42

<!DEPRECATED_MODIFIER!>impl<!> konst x get() = "Hello"

<!DEPRECATED_MODIFIER!>impl<!> object O

<!DEPRECATED_MODIFIER!>impl<!> enum class E {
    FIRST
}
