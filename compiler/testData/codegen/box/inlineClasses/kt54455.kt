// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC1(konst konstue: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC2(konst konstue: Int)

fun foo(x: IC1, y: IC2) = (x as Any) == y

fun box(): String {
    if ((IC1(1) as Any) == IC2(1)) return "Fail 1"
    if (foo(IC1(1), IC2(1))) return "Fail 2"
    return "OK"
}