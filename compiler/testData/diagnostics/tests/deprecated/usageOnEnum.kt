// FIR_IDENTICAL
// LANGUAGE: +NoDeprecationOnDeprecatedEnumEntries
// ISSUE: KT-37975

@Deprecated("")
enum class Foo(konst x: Int) {
    A(42)
}
