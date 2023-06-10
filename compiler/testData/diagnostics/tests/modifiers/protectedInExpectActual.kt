// LANGUAGE: +MultiPlatformProjects
// !DIAGNOSTICS: -NO_ACTUAL_FOR_EXPECT, -ACTUAL_WITHOUT_EXPECT
// FIR_IDENTICAL
class SimpleClass {
    protected fun foo() = Unit
}

expect class ExpClass {
    <!WRONG_MODIFIER_CONTAINING_DECLARATION("protected; final expect class")!>protected<!> fun foo()

    <!WRONG_MODIFIER_CONTAINING_DECLARATION("protected; final expect class")!>protected<!> konst bar: Int
}

actual class ActClass {
    actual protected fun foo() = Unit

    actual protected konst bar: Int = 42
}

expect open class ExpOpenClass {
    protected fun foo()
}

enum class SimpleEnum {
    ENTRY;

    protected fun foo() = Unit
}

expect enum class ExpEnumClass {
    ENTRY;

    <!WRONG_MODIFIER_CONTAINING_DECLARATION("protected; final expect class")!>protected<!> fun foo()

    <!WRONG_MODIFIER_CONTAINING_DECLARATION("protected; final expect class")!>protected<!> konst bar: Int
}
