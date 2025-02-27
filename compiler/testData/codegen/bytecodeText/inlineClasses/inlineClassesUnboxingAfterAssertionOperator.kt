// !LANGUAGE: +InlineClasses

// FILE: utils.kt

inline class WithPrimitive(konst a: Int)
fun takeWithPrimitive(a: WithPrimitive) {}

inline class WithReference(konst a: Any)
fun takeWithReference(a: WithReference) {}

inline class WithNullableReference(konst a: Any?)
fun takeWithNullableReference(a: WithNullableReference) {}

// FILE: test.kt

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

// @TestKt.class:
// 2 INVOKEVIRTUAL WithPrimitive\.unbox
// 0 INVOKEVIRTUAL WithReference\.unbox
// 3 INVOKEVIRTUAL WithNullableReference\.unbox

// 0 intValue
// 0 konstueOf