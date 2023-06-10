// FIR_IDENTICAL
// !SKIP_JAVAC
// SKIP_TXT
// !LANGUAGE: +InlineClasses, -GenericInlineClassParameter
// ALLOW_KOTLIN_PACKAGE

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Foo<T>(konst x: <!UNSUPPORTED_FEATURE!>T<!>)
@JvmInline
konstue class FooNullable<T>(konst x: <!UNSUPPORTED_FEATURE!>T?<!>)

@JvmInline
konstue class FooGenericArray<T>(konst x: <!UNSUPPORTED_FEATURE!>Array<T><!>)
@JvmInline
konstue class FooGenericArray2<T>(konst x: <!UNSUPPORTED_FEATURE!>Array<Array<T>><!>)


