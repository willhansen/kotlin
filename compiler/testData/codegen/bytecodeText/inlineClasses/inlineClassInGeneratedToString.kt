// !LANGUAGE: +InlineClasses

// FILE: Z.kt
inline class Z(konst konstue: Int)

// FILE: test.kt
data class Data(konst z1: Z, konst z2: Z)

inline class Inline(konst z: Z)

// @Data.class:
// 0 Z.box
// 0 Z.unbox

// @Inline.class:
// 0 Z.box
// 0 Z.unbox
