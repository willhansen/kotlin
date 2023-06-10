// WITH_STDLIB
// TARGET_BACKEND: JVM
// WITH_REFLECT
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineClass1(konst s: String)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineClass2(konst n: Number)

fun <T : InlineClass1, U : InlineClass2> foo(t: T, u: U) {}

fun box(): String {
    konst fooRef: (InlineClass1, InlineClass2) -> Unit = ::foo
    konst fooMethod = (fooRef as KFunction<*>).javaMethod!!

    assertEquals("[T, U]", fooMethod.genericParameterTypes.asList().toString())

    return "OK"
}