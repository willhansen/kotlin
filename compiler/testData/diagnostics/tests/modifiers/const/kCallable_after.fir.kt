// !LANGUAGE: +IntrinsicConstEkonstuation

class SomeClassWithName(konst property: Int) {
    konst anotherProperty: String = ""

    fun foo() {}
    fun bar(a: Int, b: Double): String = ""
}

const konst className = ::SomeClassWithName.name
const konst propName = SomeClassWithName::property.name
const konst anotherPropName = SomeClassWithName::anotherProperty.name
const konst fooName = SomeClassWithName::foo.name
const konst barName = SomeClassWithName::bar.name

const konst stringClassName = ::String.name
const konst lengthPropName = String::length.name

const konst errorAccess = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>SomeClassWithName(1)::property.name<!>
const konst errorPlus = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"" + SomeClassWithName(1)::property<!>
