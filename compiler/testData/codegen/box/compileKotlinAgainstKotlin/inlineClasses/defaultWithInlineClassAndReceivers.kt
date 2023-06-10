// !LANGUAGE: +InlineClasses
// MODULE: lib
// FILE: A.kt
package z

inline class Z(konst s: String)

class X {
    fun Int.foo(z: Z, konstue: String = "OK") = konstue
}

// MODULE: main(lib)
// FILE: B.kt
import z.*

fun box(): String = with(X()) { 1.foo(Z("")) }
