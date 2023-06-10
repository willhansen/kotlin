// DIAGNOSTICS: -UNUSED_PARAMETER
// !LANGUAGE: -RequiredPrimaryConstructorDelegationCallInEnums

enum class Enum1(konst a: String) {
    A;
    <!PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED_IN_ENUM!>constructor()<!>
}

enum class Enum2(konst a: String) {
    A, B;
    constructor(): this("")
}

enum class Enum3(konst a: String = "") {
    <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>A,<!> <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>B,<!> <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>C;<!>
    <!PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED_IN_ENUM!>constructor()<!>
}

enum class Enum4(konst a: String = "") {
    <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>A,<!> <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>B,<!> <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>C;<!>
    constructor(): <!CYCLIC_CONSTRUCTOR_DELEGATION_CALL!>this<!>()
}

enum class Enum5(konst a: String = "") {
    <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>A,<!> <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>B,<!> <!ENUM_ENTRY_SHOULD_BE_INITIALIZED!>C;<!>
    constructor(): this(a = "")
}

enum class Enum6(konst a: String = "") {
    A, B, C;
}

enum class Enum7(konst a: String) {
    A, B, C;
    constructor(): this(10)
    constructor(x: Int): this("")
}

enum class Enum8(konst a: String) {
    A, B, C;
    constructor(): this(10)
    <!PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED_IN_ENUM!>constructor(x: Int)<!>
}
