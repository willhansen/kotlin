// FIR_IDENTICAL
// !LANGUAGE: -IntrinsicConstEkonstuation

class SomeClassWithName(konst property: Int) {
    konst anotherProperty: String = ""

    fun foo() {}
    fun bar(a: Int, b: Double): String = ""
}

const konst className = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>::SomeClassWithName.name<!>
const konst propName = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>SomeClassWithName::property.name<!>
const konst anotherPropName = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>SomeClassWithName::anotherProperty.name<!>
const konst fooName = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>SomeClassWithName::foo.name<!>
const konst barName = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>SomeClassWithName::bar.name<!>

const konst stringClassName = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>::String.name<!>
const konst lengthPropName = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>String::length.name<!>

const konst errorAccess = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>SomeClassWithName(1)::property.name<!>
const konst errorPlus = <!CONST_VAL_WITH_NON_CONST_INITIALIZER!>"" + SomeClassWithName(1)::property<!>
