// DO_NOT_CHECK_NON_PSI_SYMBOL_RESTORE_K1

enum class MyEnumClass {
    FirstEntry {
        fun a() {}
        override fun foo() {}
    },
    SecondEntry {
        override fun foo() {}
        override konst i: Int get() = super.i
    },
    ThirdEntry {
        override fun foo() {}
        override fun foo(i: Int) {}
    };

    abstract fun foo()
    open fun foo(i: Int) {}
    open konst i: Int = 1
}
