// FIR_IDENTICAL
// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Test1(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test1<!>)

@JvmInline
konstue class Test2A(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test2B<!>)
@JvmInline
konstue class Test2B(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test2A<!>)

@JvmInline
konstue class Test3A(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test3B<!>)
@JvmInline
konstue class Test3B(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test3C<!>)
@JvmInline
konstue class Test3C(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>Test3A<!>)

@JvmInline
konstue class TestNullable(konst x: <!VALUE_CLASS_CANNOT_BE_RECURSIVE!>TestNullable?<!>)

@JvmInline
konstue class TestRecursionInTypeArguments(konst x: List<TestRecursionInTypeArguments>)

@JvmInline
konstue class TestRecursionInArray(konst x: Array<TestRecursionInArray>)

@JvmInline
konstue class TestRecursionInUpperBounds<T : TestRecursionInUpperBounds<T>>(konst x: T)

@JvmInline
konstue class Id<T>(konst x: T)
@JvmInline
konstue class TestRecursionThroughId(konst x: Id<TestRecursionThroughId>)
