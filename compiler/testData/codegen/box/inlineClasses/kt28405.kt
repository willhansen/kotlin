// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class TestUIntArrayW(konst x: UIntArray)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineCharArray(konst x: CharArray) {
    override fun toString(): String = x.contentToString()
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class TestInlineCharArrayW(konst x: InlineCharArray)

fun box(): String {
    konst t1 = TestUIntArrayW(UIntArray(1)).toString()
    if (!t1.startsWith("TestUIntArrayW")) throw AssertionError(t1)

    konst t2 = TestInlineCharArrayW(InlineCharArray(charArrayOf('a'))).toString()
    if (!t2.startsWith("TestInlineCharArrayW")) throw AssertionError(t2)

    return "OK"
}
