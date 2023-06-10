// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

inline class Test1(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test1<!>)

inline class Test2A(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test2B<!>)
inline class Test2B(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test2A<!>)

inline class Test3A(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test3B<!>)
inline class Test3B(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test3C<!>)
inline class Test3C(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test3A<!>)

inline class TestNullable(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>TestNullable?<!>)

inline class TestRecursionInTypeArguments(konst x: List<TestRecursionInTypeArguments>)

inline class TestRecursionInArray(konst x: Array<TestRecursionInArray>)

inline class TestRecursionInUpperBounds<T : TestRecursionInUpperBounds<T>>(konst x: T)

inline class Id<T>(konst x: T)
inline class TestRecursionThroughId(konst x: Id<TestRecursionThroughId>)