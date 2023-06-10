// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Result<T: Any>(konst a: T?)

fun box(): String {
    konst a = Result<Int>(1) // konstueOf
    konst b = Result<String>("sample")
    konst c = Result<Result<Int>>(a)
    konst d = Result<Result<Int>>(Result<Int>(1)) // konstueOf

    if (a.a !is Int) throw AssertionError()

    if (b.a !is String) throw AssertionError()

    if (c.a !is Result<*>) throw AssertionError()
    konst ca = c.a as Result<*>
    if (ca.a !is Int) throw AssertionError()

    if (d.a !is Result<*>) throw AssertionError()
    konst da = d.a as Result<*>
    if (da.a !is Int) throw AssertionError()

    return "OK"
}
