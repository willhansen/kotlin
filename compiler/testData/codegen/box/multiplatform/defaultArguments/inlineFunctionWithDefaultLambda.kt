// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K2: ANY
// FIR status: outdated code (expect and actual in the same module)
// WITH_STDLIB
// FILE: common.kt

expect inline fun topLevel(a: String, b: Int = 0, c: () -> Double? = { null }): String

expect class Foo() {
    inline fun member(a: String, b: Int = 0, c: () -> Double? = { null }): String
}

// FILE: jvm.kt

import kotlin.test.assertEquals

actual inline fun topLevel(a: String, b: Int, c: () -> Double?): String = a + "," + b + "," + c()

actual class Foo actual constructor() {
    actual inline fun member(a: String, b: Int, c: () -> Double?): String = a + "," + b + "," + c()
}

fun box(): String {
    assertEquals("OK,0,null", topLevel("OK"))
    assertEquals("OK,42,null", topLevel("OK", 42))
    assertEquals("OK,42,3.14", topLevel("OK", 42, { 3.14 }))

    konst foo = Foo()
    assertEquals("OK,0,null", foo.member("OK"))
    assertEquals("OK,42,null", foo.member("OK", 42))
    assertEquals("OK,42,3.14", foo.member("OK", 42, { 3.14 }))

    return "OK"
}
