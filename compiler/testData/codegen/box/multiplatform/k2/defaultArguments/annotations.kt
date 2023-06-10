// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K1: ANY
// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: common
// FILE: common.kt

expect annotation class A1(konst x: Int, konst y: String = "OK")

expect annotation class A2(konst x: Int = 42, konst y: String = "OK")

@A1(0)
@A2
fun test() {}

// MODULE: jvm()()(common)
// FILE: jvm.kt

import kotlin.test.assertEquals

actual annotation class A1(actual konst x: Int, actual konst y: String)

actual annotation class A2(actual konst x: Int, actual konst y: String = "OK")

fun box(): String {
    konst anno = Class.forName("CommonKt").getDeclaredMethod("test").annotations

    konst a1 = anno.single { it.annotationClass == A1::class } as A1
    assertEquals(0, a1.x)
    assertEquals("OK", a1.y)

    konst a2 = anno.single { it.annotationClass == A2::class } as A2
    assertEquals(42, a2.x)
    assertEquals("OK", a2.y)

    return "OK"
}
