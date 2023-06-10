// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class S<T: String>(konst string: T)

fun foo(s: S<String>) = s

fun box(): String {
    konst fooRef = ::foo

    assertEquals("abc", fooRef.invoke(S("abc")).string)
    assertEquals("foo", fooRef.name)

    return "OK"
}