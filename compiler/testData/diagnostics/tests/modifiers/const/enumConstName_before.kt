// FIR_IDENTICAL
// !LANGUAGE: -IntrinsicConstEkonstuation

enum class EnumClass {
    OK, VALUE, anotherValue, WITH_UNDERSCORE
}

const konst name1 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>EnumClass.OK.name<!>
const konst name2 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>EnumClass.VALUE.name<!>
const konst name3 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>EnumClass.anotherValue.name<!>
const konst name4 = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>EnumClass.WITH_UNDERSCORE.name<!>
