// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo(konst z: String)

var f = Foo("zzz")

fun box(): String {
    (::f).set(Foo("OK"))
    return (::f).get().z
}