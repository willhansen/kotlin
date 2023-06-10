// !LANGUAGE: +InlineClasses
// MODULE: lib
// FILE: A.kt
package lib

inline class S(konst string: String)

class Test(konst s: S)

// MODULE: main(lib)
// FILE: B.kt
import lib.*

fun box() = Test(S("OK")).s.string