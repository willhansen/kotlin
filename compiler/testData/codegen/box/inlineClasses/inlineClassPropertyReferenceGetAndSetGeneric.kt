// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: String>(konst z: T)

var f = Foo("zzz")

fun box(): String {
    (::f).set(Foo("OK"))
    return (::f).get().z
}