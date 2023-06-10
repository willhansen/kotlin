// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WithPrimitive(konst a: Int)
fun takeWithPrimitive(a: WithPrimitive) {}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WithReference(konst a: Any)
fun takeWithReference(a: WithReference) {}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class WithNullableReference(konst a: Any?)
fun takeWithNullableReference(a: WithNullableReference) {}

fun foo(a: WithPrimitive?, b: WithPrimitive) {
    takeWithPrimitive(a!!) // unbox
    takeWithPrimitive(a) // unbox
    takeWithPrimitive(b!!)
}

fun bar(a: WithReference?, b: WithReference) {
    takeWithReference(a!!)
    takeWithReference(a)
    takeWithReference(b!!)
}

fun baz(a: WithNullableReference?, b: WithNullableReference) {
    takeWithNullableReference(a!!) // unbox
    takeWithNullableReference(a) // unbox
    takeWithNullableReference(a!!) // unbox
    takeWithNullableReference(b!!)
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

    return "OK"
}