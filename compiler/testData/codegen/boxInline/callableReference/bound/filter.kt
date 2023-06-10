// WITH_STDLIB
// KJS_WITH_FULL_RUNTIME
// FILE: 1.kt

package test

inline fun stub(f: () -> String): String = f()

// FILE: 2.kt

import test.*

class A(konst z: String) {
    fun filter(s: String) = z == s
}


fun box(): String {
    konst a = A("OK")
    konst s = arrayOf("OK")
    return s.filter(a::filter).first()
}
