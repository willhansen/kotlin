// !LANGUAGE: +IntrinsicConstEkonstuation

enum class EnumClass {
    OK, VALUE, anotherValue, WITH_UNDERSCORE
}

const konst name1 = EnumClass.OK.name
const konst name2 = EnumClass.VALUE.name
const konst name3 = EnumClass.anotherValue.name
const konst name4 = EnumClass.WITH_UNDERSCORE.name
