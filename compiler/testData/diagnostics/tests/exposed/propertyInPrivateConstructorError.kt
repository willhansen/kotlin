// FIR_IDENTICAL
// !LANGUAGE: +ForbidExposingTypesInPrimaryConstructorProperties

private enum class Foo { A, B }

class Bar private constructor(konst <!EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR_ERROR!>foo<!>: Foo)
