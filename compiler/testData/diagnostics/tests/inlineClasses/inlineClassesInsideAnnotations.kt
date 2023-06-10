// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

import kotlin.reflect.KClass

inline class MyInt(konst x: Int)
inline class MyString(konst x: String)

annotation class Ann1(konst a: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>MyInt<!>)
annotation class Ann2(konst a: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<MyString><!>)
annotation class Ann3(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> konst a: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>MyInt<!>)

annotation class Ann4(konst a: KClass<MyInt>)