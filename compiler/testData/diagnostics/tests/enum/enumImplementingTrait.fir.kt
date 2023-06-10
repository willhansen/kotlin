interface T1 {
    fun foo()
}

enum class EnumImplementingTraitWithFun: T1 {
    E1 {
        override fun foo() {}
    },
    <!ABSTRACT_MEMBER_NOT_IMPLEMENTED_BY_ENUM_ENTRY!>E2<!>
}

interface T2 {
    konst bar: Int
}

enum class EnumImplementingTraitWithVal: T2 {
    E1 {
        override konst bar = 1
    },
    <!ABSTRACT_MEMBER_NOT_IMPLEMENTED_BY_ENUM_ENTRY!>E2<!>
}
