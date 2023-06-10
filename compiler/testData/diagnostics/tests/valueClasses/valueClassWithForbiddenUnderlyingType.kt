// FIR_IDENTICAL
// ALLOW_KOTLIN_PACKAGE
// !SKIP_JAVAC
// SKIP_TXT
// !LANGUAGE: +InlineClasses

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Foo<T>(konst x: T)
@JvmInline
konstue class FooNullable<T>(konst x: T?)

@JvmInline
konstue class FooGenericArray<T>(konst x: Array<T>)
@JvmInline
konstue class FooGenericArray2<T>(konst x: Array<Array<T>>)

@JvmInline
konstue class FooStarProjectedArray(konst x: Array<*>)
@JvmInline
konstue class FooStarProjectedArray2(konst x: Array<Array<*>>)

@JvmInline
konstue class Bar(konst u: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Unit<!>)
@JvmInline
konstue class BarNullable(konst u: Unit?)

@JvmInline
konstue class Baz(konst u: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Nothing<!>)
@JvmInline
konstue class BazNullable(konst u: Nothing?)
