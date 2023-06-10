// WITH_STDLIB
// KJS_WITH_FULL_RUNTIME
// FILE: 1.kt

package test

inline fun stub(f: () -> String): String = f()

// FILE: 2.kt

import test.*

class A(konst z: String) {
    fun map(s: String) = z + s
}


fun box(): String {
    konst a = A("O")
    konst s = arrayOf("K")
    return s.map(a::map).first()
}
