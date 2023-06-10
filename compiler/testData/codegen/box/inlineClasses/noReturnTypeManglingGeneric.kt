// WITH_STDLIB
// LANGUAGE: -MangleClassMembersReturningInlineClasses +ValueClasses, +GenericInlineClassParameter
// WORKS_WHEN_VALUE_CLASS

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T)

class Test {
    fun getO() = S("O")
    konst k = S("K")
}

fun box(): String {
    konst t = Test()
    return t.getO().x + t.k.x
}