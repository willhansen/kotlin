// !LANGUAGE: +MultiPlatformProjects
// MODULE: m1-common
// FILE: common.kt
// TODO: .fir.kt version is just a stub.

expect interface My {
    open fun bar()
    <!EXPECTED_DECLARATION_WITH_BODY!>open fun bas()<!> {}
    <!REDUNDANT_MODIFIER!>open<!> abstract fun bat(): Int

    fun foo()


    open konst a: Int
    open konst b: String
    open konst c: String get() = ""
    <!REDUNDANT_MODIFIER!>open<!> abstract konst e: Int

    konst f: Int
}

class MyImpl1: My

class MyImpl2: My {
    override fun foo() {}
    override konst f = 0
    override konst e = 1
}

expect interface Outer {
    interface Inner {
        open fun bar()
        <!EXPECTED_DECLARATION_WITH_BODY!>open fun bas()<!> {}
        <!REDUNDANT_MODIFIER!>open<!> abstract fun bat(): Int
        fun foo()
    }
}
