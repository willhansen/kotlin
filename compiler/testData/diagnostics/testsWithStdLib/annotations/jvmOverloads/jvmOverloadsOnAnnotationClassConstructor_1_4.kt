// FIR_IDENTICAL
// !LANGUAGE: +ProhibitJvmOverloadsOnConstructorsOfAnnotationClasses

annotation class A1 <!OVERLOADS_ANNOTATION_CLASS_CONSTRUCTOR_ERROR!>@JvmOverloads<!> constructor(konst x: Int = 1)
annotation class A2 <!OVERLOADS_ANNOTATION_CLASS_CONSTRUCTOR_ERROR!>@JvmOverloads<!> constructor()
