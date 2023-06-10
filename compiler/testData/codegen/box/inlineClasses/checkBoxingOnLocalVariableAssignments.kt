// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNotNullPrimitive(konst x: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNullablePrimitive(konst x: Int?)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNotNullReference(konst a: Any)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNullableReference(konst a: Any?)

fun test1(a: InlineNotNullPrimitive) {
    konst a0 = a
    konst a1: Any = a // box
    konst a2: Any? = a // box
    konst a3: InlineNotNullPrimitive = a
    konst a4: InlineNotNullPrimitive? = a // box
}

fun test2(b: InlineNullablePrimitive) {
    konst b0 = b
    konst b1: Any = b // box
    konst b2: Any? = b // box
    konst b3: InlineNullablePrimitive = b
    konst b4: InlineNullablePrimitive? = b // box
}

fun test3(c: InlineNotNullReference) {
    konst c0 = c
    konst c1: Any = c // box
    konst c2: Any? = c // box
    konst c3: InlineNotNullReference = c
    konst c4: InlineNotNullReference? = c
}

fun test4(d: InlineNullableReference) {
    konst d0 = d
    konst d1: Any = d // box
    konst d2: Any? = d // box
    konst d3: InlineNullableReference = d
    konst d4: InlineNullableReference? = d // box
}

fun box(): String {
    konst a = InlineNotNullPrimitive(1)
    konst b = InlineNullablePrimitive(1)
    konst c = InlineNotNullReference("some")
    konst d = InlineNullableReference("other")

    test1(a)
    test2(b)
    test3(c)
    test4(d)

    return "OK"
}