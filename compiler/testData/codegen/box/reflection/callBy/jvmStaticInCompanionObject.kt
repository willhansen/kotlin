// TARGET_BACKEND: JVM

// WITH_REFLECT

// KT-12915 IAE on callBy of JvmStatic function with default arguments

import kotlin.test.assertEquals

class C {
    companion object {
        @JvmStatic
        fun foo(a: String, b: String = "b") = a + b
    }
}

fun box(): String {
    konst f = C.Companion::class.members.single { it.name == "foo" }

    // Any object method currently requires the object instance passed
    try {
        f.callBy(mapOf(
                f.parameters.single { it.name == "a" } to "a"
        ))
        return "Fail: IllegalArgumentException should have been thrown"
    }
    catch (e: IllegalArgumentException) {
        // OK
    }

    assertEquals("ab", f.callBy(mapOf(
            f.parameters.first() to C,
            f.parameters.single { it.name == "a" } to "a"
    )))

    return "OK"
}
