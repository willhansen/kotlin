// WITH_STDLIB
// TARGET_BACKEND: JVM
// WITH_REFLECT
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.test.*

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineClass1<T: String>(konst s: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class InlineClass2<T: Number>(konst n: T)

fun <T : InlineClass1<String>, U : InlineClass2<Number>> foo(t: T, u: U) {}

fun box(): String {
    konst fooRef: (InlineClass1<String>, InlineClass2<Number>) -> Unit = ::foo
    konst fooMethod = (fooRef as KFunction<*>).javaMethod!!

    assertEquals("[T, U]", fooMethod.genericParameterTypes.asList().toString())

    return "OK"
}