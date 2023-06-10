// FIR_IDENTICAL
enum class EnumWithOpenMembers {
    E1 {
        override fun foo() = 1
        override konst bar: String = "a"
    },

    E2 {
        <!OVERRIDING_FINAL_MEMBER!>override<!> fun f() = 3
        <!OVERRIDING_FINAL_MEMBER!>override<!> konst b = 4
    };

    open fun foo() = 1
    open konst bar: String = ""

    fun f() = 2
    konst b = 3
}