// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import java.lang.reflect.Modifier

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC1 public constructor(konst i: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC11 internal constructor(konst i: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC2 private constructor(konst i: Int)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC4 protected constructor(konst i: Int)

fun box(): String {
    if (!Modifier.isPublic(IC1::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 1"
    if (!Modifier.isPublic(IC11::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 1"
    if (!Modifier.isPrivate(IC2::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 2"
    if (!Modifier.isProtected(IC4::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 4"
    return "OK"
}