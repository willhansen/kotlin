// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: 1.kt

package test

class Foo(@JvmField konst a: String)

inline fun test(s: (Foo) -> String): String {
    return s(Foo("OK"))
}

// FILE: 2.kt

import test.*

fun box(): String {
    return test(Foo::a)
}
