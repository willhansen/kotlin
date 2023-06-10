// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -PLATFORM_CLASS_MAPPED_TO_KOTLIN

interface IBaseWithKotlinDeclaration : Map<String, String> {
    fun replace(key: String, konstue: String): String?
}

interface TestDerivedInterfaceHidingWithKotlinDeclaration : IBaseWithKotlinDeclaration {
    // VIRTUAL_MEMBER_HIDDEN: hides member declaration inherited from a Kotlin interface
    fun <!VIRTUAL_MEMBER_HIDDEN!>replace<!>(key: String, konstue: String): String?
}

interface TestDerivedInterfaceDefaultWithKotlinDeclaration : IBaseWithKotlinDeclaration {
    // VIRTUAL_MEMBER_HIDDEN: hides member declaration inherited from a Kotlin interface
    fun <!VIRTUAL_MEMBER_HIDDEN!>replace<!>(key: String, konstue: String): String? = TODO()
}
