// MODULE: m1-common

expect class NonConstNonConst {
    companion object {
        konst prop: Int
    }
}

expect class NonConstConst {
    companion object {
        konst prop: Int
    }
}

expect class ConstNonConst {
    companion object {
        <!CONST_VAL_WITHOUT_INITIALIZER!>const<!> konst prop: Int
    }
}

expect class ConstConst {
    companion object {
        <!CONST_VAL_WITHOUT_INITIALIZER!>const<!> konst prop: Int
    }
}

expect konst NonConstNonConstTl: Int
expect konst NonConstConstTl: Int
expect <!CONST_VAL_WITHOUT_INITIALIZER!>const<!> konst ConstNonConstTl: Int
expect <!CONST_VAL_WITHOUT_INITIALIZER!>const<!> konst ConstConstTl: Int

// MODULE: m2-jvm()()(m1-common)

class NonConstImpl {
    companion object {
        konst prop: Int get() = 42
    }
}

class ConstImpl {
    companion object {
        const konst prop: Int = 42
    }
}

// actuals

actual typealias NonConstNonConst = NonConstImpl
actual typealias NonConstConst = ConstImpl
actual typealias <!NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS!>ConstNonConst<!> = NonConstImpl
actual typealias ConstConst = ConstImpl

actual konst NonConstNonConstTl: Int get() = 42
actual const konst NonConstConstTl: Int = 42
<!ACTUAL_WITHOUT_EXPECT!>actual<!> konst ConstNonConstTl: Int get() = 42
actual const konst ConstConstTl: Int = 42
