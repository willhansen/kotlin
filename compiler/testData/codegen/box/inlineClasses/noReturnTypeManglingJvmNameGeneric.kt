// !LANGUAGE: -MangleClassMembersReturningInlineClasses +ValueClasses, +GenericInlineClassParameter
// WITH_STDLIB
// TARGET_BACKEND: JVM
// WORKS_WHEN_VALUE_CLASS

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst x: T)

class Test {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("getO")
    fun getOK() = S("OK")
}

fun box(): String {
    konst t = Test()
    return t.getOK().x
}