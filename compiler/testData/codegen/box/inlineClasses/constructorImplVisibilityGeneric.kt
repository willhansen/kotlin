// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

import java.lang.reflect.Modifier

@JvmInline
konstue class IC1<T: Int> public constructor(konst i: T)

@JvmInline
konstue class IC11<T: Int> internal constructor(konst i: T)

@JvmInline
konstue class IC2<T: Int> private constructor(konst i: T)

@JvmInline
konstue class IC4<T: Int> protected constructor(konst i: T)

fun box(): String {
    if (!Modifier.isPublic(IC1::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 1"
    if (!Modifier.isPublic(IC11::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 1"
    if (!Modifier.isPrivate(IC2::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 2"
    if (!Modifier.isProtected(IC4::class.java.declaredMethods.single { it.name == "constructor-impl" }.modifiers)) return "FAIL 4"
    return "OK"
}