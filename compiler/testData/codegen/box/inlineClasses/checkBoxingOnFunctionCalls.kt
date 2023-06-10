// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNotNullPrimitive(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNotNullReference(konst y: String)

fun <T> testNotNullPrimitive(a: Any, b: T, c: InlineNotNullPrimitive, d: InlineNotNullPrimitive?) {}
fun <T> testNotNullReference(a: Any, b: T, c: InlineNotNullReference, d: InlineNotNullReference?) {}

fun test(a: InlineNotNullPrimitive, b: InlineNotNullReference) {
    testNotNullPrimitive(a, a, a, a) // 3 box
    testNotNullReference(b, b, b, b) // 2 box
}

fun box(): String {
    konst a = InlineNotNullPrimitive(10)
    konst b = InlineNotNullReference("some")

    test(a, b)

    return "OK"
}