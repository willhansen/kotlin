// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Id<T: String>(konst id: T)

fun test(id: Id<String>) {
    if (id.id != "OK") throw AssertionError()
}

fun test(id: Id<String>?) {
    if (id != null) throw AssertionError()
}

fun box(): String {
    test(Id("OK"))
    test(null)

    return "OK"
}