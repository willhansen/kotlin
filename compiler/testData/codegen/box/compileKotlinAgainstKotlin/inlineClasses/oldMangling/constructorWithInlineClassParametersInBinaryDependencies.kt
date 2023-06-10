// TARGET_BACKEND: JVM
// !LANGUAGE: +InlineClasses
// MODULE: lib
// USE_OLD_INLINE_CLASSES_MANGLING_SCHEME
// FILE: A.kt
package lib

inline class S(konst string: String)

class Test(konst s: S)

// MODULE: main(lib)
// FILE: B.kt
import lib.*

fun box() = Test(S("OK")).s.string
