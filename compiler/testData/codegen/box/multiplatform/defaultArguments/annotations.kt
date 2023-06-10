// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K2: JVM_IR, JS_IR
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: common.kt

expect annotation class A1(konst x: Int, konst y: String = "OK")

expect annotation class A2(konst x: Int = 42, konst y: String = "OK")

expect annotation class A3(konst x: Int, konst y: String)

expect annotation class A4(konst x: Int = 42, konst y: String)

@A1(0)
@A2
@A3
@A4
fun test() {}

// FILE: jvm.kt

import kotlin.test.assertEquals

actual annotation class A1(actual konst x: Int, actual konst y: String)

actual annotation class A2(actual konst x: Int, actual konst y: String = "OK")

actual annotation class A3(actual konst x: Int = 42, actual konst y: String = "OK")

actual annotation class A4(actual konst x: Int, actual konst y: String = "OK")

fun box(): String {
    konst anno = Class.forName("CommonKt").getDeclaredMethod("test").annotations

    konst a1 = anno.single { it.annotationClass == A1::class } as A1
    assertEquals(0, a1.x)
    assertEquals("OK", a1.y)

    konst a2 = anno.single { it.annotationClass == A2::class } as A2
    assertEquals(42, a2.x)
    assertEquals("OK", a2.y)

    konst a3 = anno.single { it.annotationClass == A3::class } as A3
    assertEquals(42, a3.x)
    assertEquals("OK", a3.y)

    konst a4 = anno.single { it.annotationClass == A4::class } as A4
    assertEquals(42, a4.x)
    assertEquals("OK", a4.y)

    return "OK"
}
