// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst a: Any)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class NA(konst b: Any?)

fun box(): String {
    konst ns1 = NA(A("abc"))
    konst ns2 = NA(null)
    konst t = "-$ns1-$ns2-"
    if (t != "-NA(b=A(a=abc))-NA(b=null)-") return throw AssertionError(t)
    return "OK"
}