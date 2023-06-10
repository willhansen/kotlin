// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNotNullPrimitive<T: Int>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNullablePrimitive<T: Int?>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNotNullReference<T: Any>(konst a: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNullableReference<T>(konst a: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineNullableReference2<T: Any>(konst a: T?)

fun <T: Int> test1(a: InlineNotNullPrimitive<T>) {
    konst a0 = a
    konst a1: Any = a // box
    konst a2: Any? = a // box
    konst a3: InlineNotNullPrimitive<T> = a
    konst a4: InlineNotNullPrimitive<T>? = a // box
}

fun <T: Int?> test2(b: InlineNullablePrimitive<T>) {
    konst b0 = b
    konst b1: Any = b // box
    konst b2: Any? = b // box
    konst b3: InlineNullablePrimitive<T> = b
    konst b4: InlineNullablePrimitive<T>? = b // box
}

fun <T: Any> test3(c: InlineNotNullReference<T>) {
    konst c0 = c
    konst c1: Any = c // box
    konst c2: Any? = c // box
    konst c3: InlineNotNullReference<T> = c
    konst c4: InlineNotNullReference<T>? = c
}

fun <T> test4(d: InlineNullableReference<T>) {
    konst d0 = d
    konst d1: Any = d // box
    konst d2: Any? = d // box
    konst d3: InlineNullableReference<T> = d
    konst d4: InlineNullableReference<T>? = d // box
}

fun <T: Any> test5(e: InlineNullableReference2<T>) {
    konst e0 = e
    konst e1: Any = e // box
    konst e2: Any? = e // box
    konst e3: InlineNullableReference2<T> = e
    konst e4: InlineNullableReference2<T>? = e // box
}

fun box(): String {
    konst a = InlineNotNullPrimitive(1)
    konst b = InlineNullablePrimitive(1)
    konst c = InlineNotNullReference("some")
    konst d = InlineNullableReference("other")
    konst e = InlineNullableReference2("other2")

    test1(a)
    test2(b)
    test3(c)
    test4(d)
    test5(e)

    return "OK"
}