// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WithPrimitive<T: Int>(konst a: T)
fun <T: Int> takeWithPrimitive(a: WithPrimitive<T>) {}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WithReference<T: Any>(konst a: T)
fun <T: Any> takeWithReference(a: WithReference<T>) {}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WithNullableReference<T>(konst a: T)
fun <T> takeWithNullableReference(a: WithNullableReference<T>) {}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WithNullableReference2<T: Any>(konst a: T?)
fun <T: Any> takeWithNullableReference2(a: WithNullableReference2<T>) {}

fun <T: Int> foo(a: WithPrimitive<T>?, b: WithPrimitive<T>) {
    takeWithPrimitive(a!!) // unbox
    takeWithPrimitive(a) // unbox
    takeWithPrimitive(b!!)
}

fun <T: Any, T2: Any> bar(a: WithReference<T>?, b: WithReference<T2>) {
    takeWithReference(a!!)
    takeWithReference(a)
    takeWithReference(b!!)
}

fun <T, R> baz(a: WithNullableReference<T>?, b: WithNullableReference<R>) {
    takeWithNullableReference(a!!) // unbox
    takeWithNullableReference(a) // unbox
    takeWithNullableReference(a!!) // unbox
    takeWithNullableReference(b!!)
}

fun <T: Any, R: Any> baz2(a: WithNullableReference2<T>?, b: WithNullableReference2<R>) {
    takeWithNullableReference2(a!!) // unbox
    takeWithNullableReference2(a) // unbox
    takeWithNullableReference2(a!!) // unbox
    takeWithNullableReference2(b!!)
}

fun box(): String {
    konst a1 = WithPrimitive(1)
    konst b1 = WithPrimitive(2)

    foo(a1, b1)

    konst a2 = WithReference("")

    bar(a2, a2)

    konst a3 = WithNullableReference("test")
    konst a4 = WithNullableReference(123)

    baz(a3, a4)

    konst a32 = WithNullableReference2("test")
    konst a42 = WithNullableReference2(123)

    baz2(a32, a42)

    return "OK"
}