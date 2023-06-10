// MODULE: m1-common
// FILE: common.kt
// TODO: .fir.kt version is just a stub.
expect interface My {
    open fun openFunPositive()
    open fun openFunNegative()
    abstract fun abstractFun()

    open konst openValPositive: Int
    open konst openValNegative: Int
    abstract konst abstractVal: Int
}

// MODULE: m1-jvm()()(m1-common)
// FILE: jvm.kt
actual interface My {
    actual fun openFunPositive() = Unit
    <!ACTUAL_WITHOUT_EXPECT!>actual fun openFunNegative()<!>
    actual fun abstractFun()

    actual konst openValPositive: Int get() = 0
    <!ACTUAL_WITHOUT_EXPECT!>actual konst openValNegative: Int<!>
    actual konst abstractVal: Int
}
