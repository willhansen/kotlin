// FIR_IDENTICAL
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
// LANGUAGE: +ValueClasses

@JvmInline
konstue class Empty<T><!VALUE_CLASS_EMPTY_CONSTRUCTOR!>()<!>


@JvmInline
konstue class Foo<T>(konst x: T, konst y: T)

@JvmInline
konstue class FooNullable<T>(konst x: T?, konst y: T?)


@JvmInline
konstue class FooGenericArray<T>(konst x: Array<T>, konst y: Array<T>)

@JvmInline
konstue class FooGenericArray2<T>(konst x: Array<Array<T>>, konst y: Array<Array<T>>)


@JvmInline
konstue class FooStarProjectedArray(konst x: Array<*>, konst y: Array<*>)

@JvmInline
konstue class FooStarProjectedArray2(konst x: Array<Array<*>>, konst y: Array<Array<*>>)


@JvmInline
konstue class Bar(konst u: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Unit<!>, konst y: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Unit<!>)

@JvmInline
konstue class BarNullable(konst u: Unit?, konst y: Unit?)


@JvmInline
konstue class Baz(konst u: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Nothing<!>, konst y: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Nothing<!>)

@JvmInline
konstue class Baz1(konst u: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Nothing<!>, konst y: Int)

@JvmInline
konstue class Baz2(konst u: Int, konst y: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Nothing<!>)

@JvmInline
konstue class BazNullable(konst u: Nothing?, konst y: Nothing?)
