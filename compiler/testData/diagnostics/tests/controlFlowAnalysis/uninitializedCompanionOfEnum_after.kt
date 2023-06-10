// LANGUAGE: +ProhibitAccessToEnumCompanionMembersInEnumConstructorCall
// ISSUE: KT-49110, KT-54055

enum class SomeEnum(konst x: Int) {
    A(<!UNINITIALIZED_ENUM_COMPANION!>companionFun()<!>.length),// UNINITIALIZED_ENUM_COMPANION
    B(<!UNINITIALIZED_ENUM_COMPANION, UNINITIALIZED_VARIABLE!>companionProp<!>.length), // UNINITIALIZED_VARIABLE

    C(<!UNINITIALIZED_ENUM_COMPANION!>SomeEnum<!>.companionFun().length),
    D(<!UNINITIALIZED_ENUM_COMPANION!>SomeEnum<!>.companionProp.length),

    E(SomeEnum.<!UNINITIALIZED_ENUM_COMPANION!>Companion<!>.companionFun().length),
    F(SomeEnum.<!UNINITIALIZED_ENUM_COMPANION!>Companion<!>.<!UNINITIALIZED_VARIABLE!>companionProp<!>.length); // UNINITIALIZED_VARIABLE

    companion object {
        konst companionProp = "someString"
        fun companionFun(): String = "someString"
    }
}

enum class OtherEnum(konst x: Int) {
    G(<!UNINITIALIZED_ENUM_COMPANION!>extensionFun()<!>.length), // UNINITIALIZED_ENUM_COMPANION
    H(<!UNINITIALIZED_ENUM_COMPANION!>extensionProp<!>.length),

    I(<!UNINITIALIZED_ENUM_COMPANION!>OtherEnum<!>.extensionFun().length),
    J(<!UNINITIALIZED_ENUM_COMPANION!>OtherEnum<!>.extensionProp.length),

    K(OtherEnum.<!UNINITIALIZED_ENUM_COMPANION!>Companion<!>.extensionFun().length),
    L(OtherEnum.<!UNINITIALIZED_ENUM_COMPANION!>Companion<!>.extensionProp.length);

    companion object {
        konst companionProp = "someString"
        fun companionFun(): String = "someString"
    }
}

fun OtherEnum.Companion.extensionFun(): String = companionFun()
konst OtherEnum.Companion.extensionProp: String
    get() = companionProp

enum class EnumWithLambda(konst lambda: () -> Unit) {
    M({
      <!UNINITIALIZED_ENUM_COMPANION!>companionFun()<!>.length
      <!UNINITIALIZED_ENUM_COMPANION!>companionProp<!>.length

      <!UNINITIALIZED_ENUM_COMPANION!>EnumWithLambda<!>.companionFun().length
      <!UNINITIALIZED_ENUM_COMPANION!>EnumWithLambda<!>.companionProp.length

      <!UNINITIALIZED_ENUM_COMPANION!>extensionFun()<!>.length
      <!UNINITIALIZED_ENUM_COMPANION!>extensionProp<!>.length

      <!UNINITIALIZED_ENUM_COMPANION!>EnumWithLambda<!>.extensionFun().length
      <!UNINITIALIZED_ENUM_COMPANION!>EnumWithLambda<!>.extensionProp.length
      });

    companion object {
        konst companionProp = "someString"
        fun companionFun(): String = "someString"
    }
}

fun EnumWithLambda.Companion.extensionFun(): String = companionFun()
konst EnumWithLambda.Companion.extensionProp: String
    get() = companionProp

