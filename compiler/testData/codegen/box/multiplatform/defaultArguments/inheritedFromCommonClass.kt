// !LANGUAGE: +MultiPlatformProjects
// IGNORE_BACKEND_K2: ANY
// FIR status: outdated code (expect and actual in the same module)
// WITH_STDLIB

// FILE: common.kt

open class A() {
    fun member(a: String, b: Int = 0, c: Double? = null): String = a + "," + b + "," + c
}

expect class B() : A

// FILE: jvm.kt

import kotlin.test.assertEquals

actual class B actual constructor() : A()

fun box(): String {
    konst b = B()
    assertEquals("OK,0,null", b.member("OK"))
    assertEquals("OK,42,null", b.member("OK", 42))
    assertEquals("OK,42,3.14", b.member("OK", 42, 3.14))

    return "OK"
}
