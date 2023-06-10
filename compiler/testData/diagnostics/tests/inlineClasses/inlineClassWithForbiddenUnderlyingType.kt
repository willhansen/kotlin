// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

inline class Foo<T>(konst x: T)
inline class FooNullable<T>(konst x: T?)

inline class FooGenericArray<T>(konst x: Array<T>)
inline class FooGenericArray2<T>(konst x: Array<Array<T>>)

inline class FooStarProjectedArray(konst x: Array<*>)
inline class FooStarProjectedArray2(konst x: Array<Array<*>>)

inline class Bar(konst u: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Unit<!>)
inline class BarNullable(konst u: Unit?)

inline class Baz(konst u: <!VALUE_CLASS_HAS_INAPPLICABLE_PARAMETER_TYPE!>Nothing<!>)
inline class BazNullable(konst u: Nothing?)
